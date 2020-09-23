/*
 * Copyright (c) 2020 Fraunhofer FOKUS and others. All rights reserved.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.mosaic.lib.database.persistence;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This tests the access class for SQLite files. At least reading and writing needs to be checked.
 */
public class SQLiteAccessTest extends SQLiteAccess {

    private final String butzbachDB = "/butzbach_outdated.db";
    private SQLiteAccess testDb;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Before
    public void setUp() throws Exception {
        // create a temporary copy
        File databaseCopy = folder.newFile("test.db");
        try (InputStream in = this.getClass().getResourceAsStream(butzbachDB)) {
            Files.copy(in, databaseCopy.toPath(), REPLACE_EXISTING);
        }

        // open access to copied db
        testDb = new SQLiteAccess(databaseCopy.getAbsolutePath());
        try {
            testDb.connect();
        } catch (SQLException e) {
            log.error("Error loading JDBC driver" + e.getMessage());
        }

//        stat = dbConnection.createStatement();
    }

    /**
     * Test automatic access to an existing file (independent from it having content)
     */
    @Test
    public void testSimpleDBConnection() {
        try {
            SQLiteAccess connectionDB = new SQLiteAccess(dbName);

            assertNotNull("database object wasn't created", connectionDB);
            assertNotNull("database connection wasn't created", connectionDB.dbConnection);
            assertFalse("database connection is closed, but shouldn't be", connectionDB.dbConnection.isClosed());

            connectionDB.disconnect(null);
            assertTrue("connection wasn't closed", connectionDB.dbConnection.isClosed());

        } catch (SQLException e) {
            log.error("error while accessing test database: {}", e.getMessage());
            fail("unexpected exception while creating database connection");
        }
    }

    /**
     * Test manual access to an existing file (independent of it having content).
     */
    @Test
    public void testManualDBConnection() {
        try {
            SQLiteAccess connectionDB = new SQLiteAccess();
            assertNotNull("database object wasn't created", connectionDB);

            connectionDB.setDatabaseFile(dbName);
            assertNotNull("connection wasn't created", connectionDB.dbConnection);
            // TODO: is this the way to go? or simply open and thats it?
            assertTrue("connection is open, but should still be closed", connectionDB.dbConnection.isClosed());

            Statement statement = connectionDB.connect();
            assertFalse("connection shouldn't be closed", connectionDB.dbConnection.isClosed());

            connectionDB.disconnect(statement);
            assertTrue("connection wasn't closed after disconnection", connectionDB.dbConnection.isClosed());

        } catch (SQLException e) {
            log.error("error while accessing test database: {}", e.getMessage());
            fail("unexpected exception while creating database connection");
        }
    }

    /**
     * This should test if the optimizations can be effectively activated.
     */
    @Test
    public void testOptimizeConnection() {
        try {
            testDb.optimizeConnection();
        } catch (SQLException e) {
            fail("error while trying to apply optimizations: " + e.getMessage());
        }
    }

    /**
     * Test if statements can be resolved.
     */
    @Test
    public void testExecuteStatement() {
        try {
            Result testResult = testDb.executeStatement("SELECT name FROM sqlite_master WHERE type='table' and name='nodes' ORDER BY name;");
            assertNotNull("statement didn't create any result", testResult);
            assertNotNull("created result isn't valid", testResult.getRows());
            assertEquals("Wrong result content amount", 1, testResult.getRows().size());
            assertEquals("result content isn't valid", "nodes", testResult.getFirstRow().getString("name"));

        } catch (SQLException e) {
            log.error("error while trying to execute simple sql statement");
            fail("error while trying to execute simple sql statement" + e.getMessage());
        }
    }

}
