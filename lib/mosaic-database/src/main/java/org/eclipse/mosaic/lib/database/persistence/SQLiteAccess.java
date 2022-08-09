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
 *
 * Contact: mosaic@fokus.fraunhofer.de
 */

package org.eclipse.mosaic.lib.database.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Generalizes DB access.
 * Static call of SQL statements.
 */
class SQLiteAccess {
    private Connection dbConnection;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private String dbName = "";

    /**
     * Constructor for accessing an existing database file.
     *
     * @param filename name of SQLite database file.
     */
    protected SQLiteAccess(String filename) {
        try {
            dbName = filename;
            dbConnection = DriverManager.getConnection("jdbc:sqlite:" + filename);
        } catch (SQLException e) {
            throw new RuntimeException("unexpected error while accessing db file", e);
        }
    }

    Connection getConnection() {
        return dbConnection;
    }

    /**
     * Constructor for accessing new or existing databases.
     * Make sure to call {@link #setDatabaseFile(java.lang.String)} before use of object.
     */
    protected SQLiteAccess() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            this.log.error("Error loading JDBC driver: {}", e.getMessage());
            throw new RuntimeException("could not find JDBC driver", e);
        }
    }

    /**
     * This defines the name of the database file that will be used to read from/write to.
     * Also tests whether a connection is possible.
     *
     * @param dbName Database name to set.
     */
    protected void setDatabaseFile(String dbName) {
        this.dbName = dbName;
        try {
            Statement statement = connect();
            //this.statement.execute("PRAGMA journal_mode = OFF");
            disconnect(statement);
        } catch (SQLException e) {
            this.log.error("Error while trying to establish a connection: {}", e.getMessage());
            throw new RuntimeException("Error while trying to establish a connection", e);
        }
    }

    /**
     * Connects to the database defined by {@link #setDatabaseFile(java.lang.String)}.
     *
     * @throws SQLException thrown if connection couldn't be established of the statement couldn't be created
     */
    protected Statement connect() throws SQLException {
        if (dbConnection == null || dbConnection.isClosed()) {
            dbConnection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
        }
        return dbConnection.createStatement();
    }

    /**
     * Disconnects from a DB.
     */
    protected void disconnect(Statement statement) {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                if (statement != null && !statement.isClosed()) {
                    statement.close();
                }
                dbConnection.close();
            }
        } catch (SQLException e) {
            log.error("Error while closing SQLite database:" + e.getLocalizedMessage());
        }
    }

    /**
     * Accepts and executes SQL statements. Connects and disconnects each time.
     * Name of SQLite database file must be
     * known by call of setDbName(String).
     *
     * @param statementString SQL statement to be executed
     * @throws SQLException Exception that provides information on a database access error or other errors.
     */
    protected Result executeStatement(String statementString) throws SQLException {
        ResultSet resultSet;


        Statement statement = connect();
        statement.execute(statementString);

        // collect results
        try {
            resultSet = statement.getResultSet();
        } catch (SQLException e) {
            log.error("Error while retrieving SQL result (see next 2 lines)!");
            log.error("- statement: '{}'", statementString);
            log.error("- error message: {}", e.getMessage());
            disconnect(statement);
            return null;
        }

        // now run result through the refiner
        Result result = new Result();
        try {
            result.rows.addAll(readRows(resultSet));
        } catch (SQLException sqlE) {
            log.error("Error in collecting results: {}", sqlE.getMessage());
        }

        // close connection (already includes a statement.close())
        disconnect(statement);
        return result;
    }

    private List<ResultRow> readRows(ResultSet rs) throws SQLException {
        List<ResultRow> re = new ArrayList<>();

        final Map<String, Integer> columnIndex = new HashMap<>();
        final ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            columnIndex.put(metaData.getColumnName(i + 1), i);
        }

        while (rs.next()) {
            Object[] fields = new Object[columnIndex.size()];
            for (int column : columnIndex.values()) {
                fields[column] = rs.getObject(column + 1);
            }
            re.add(new ResultRow(columnIndex, fields));
        }
        return re;
    }

    static class ResultRow {

        private final Object[] fields;
        private final Map<String, Integer> columnIndex;

        private ResultRow(Map<String, Integer> columnIndex, Object[] fields) {
            this.columnIndex = columnIndex;
            this.fields = fields;
        }

        protected <T> T get(String columnName, T defaultValue, Function<Object, T> objectMapper) {
            Integer column = columnIndex.get(columnName);
            if (column != null) {
                Object value = fields[column];
                if (value == null) {
                    return defaultValue;
                }
                return objectMapper.apply(value);
            }
            throw new IllegalArgumentException("Invalid column name " + columnName);
        }

        public String getString(String columnName) {
            return Objects.requireNonNull(getString(columnName, null), "Value in field " + columnName + " must be non null.");
        }

        public String getString(String columnName, String defaultValue) {
            return get(columnName, defaultValue, SQLiteAccess::getAsString);
        }

        public Integer getInt(String columnName) {
            return Objects.requireNonNull(getInt(columnName, null), "Value in field " + columnName + " must be non null.");
        }

        public Integer getInt(String columnName, Integer defaultValue) {
            return get(columnName, defaultValue, SQLiteAccess::getAsInteger);
        }

        public Double getDouble(String columnName) {
            return Objects.requireNonNull(getDouble(columnName, null), "Value in field " + columnName + " must be non null.");
        }

        public Double getDouble(String columnName, Double defaultValue) {
            return get(columnName, defaultValue, SQLiteAccess::getAsDouble);
        }

        public Boolean getBoolean(String columnName) {
            return Objects.requireNonNull(getBoolean(columnName, null), "Value in field " + columnName + " must be non null.");
        }

        public Boolean getBoolean(String columnName, Boolean defaultValue) {
            return get(columnName, defaultValue, SQLiteAccess::getAsBoolean);
        }
    }

    static class EmptyRow extends ResultRow {

        private EmptyRow() {
            super(null, null);
        }

        @Override
        protected <T> T get(String columnName, T defaultValue, Function<Object, T> objectMapper) {
            return null;
        }
    }

    /**
     * Container for SQL statement results, so results remain accessible even
     * after disconnect.
     */
    static class Result {
        private final List<ResultRow> rows = new ArrayList<>();

        public List<ResultRow> getRows() {
            return rows;
        }

        public ResultRow getFirstRow() {
            if (rows.isEmpty()) {
                return new EmptyRow();
            }
            return rows.get(0);
        }
    }

    /**
     * This tries to apply the correct cast to the given object.
     *
     * @param o Object to string.
     * @return String representation of the given object.
     */
    private static String getAsString(Object o) {

        if (o instanceof Integer) {
            return String.valueOf(o);
        } else if (o instanceof Long) {
            return String.valueOf(o);
        } else if (o instanceof String) {
            return ((String) o);
        } else if (o instanceof Float) {
            return String.valueOf(o);
        } else if (o instanceof Double) {
            return String.valueOf(o);
        }

        throw new IllegalArgumentException("o unknown instanceof. o.getClass():" + o.getClass());
    }

    /**
     * This tries to apply the correct cast to the given object.
     *
     * @param o Object to cast to an Integer Object.
     * @return Representation of the object as Integer.
     */
    private static Integer getAsInteger(Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof String) {
            return Integer.parseInt((String) o);
        }

        throw new IllegalArgumentException("o unknown instanceof. o.getClass():" + o.getClass());
    }

    /**
     * This tries to cast the given object to a Double object.
     *
     * @param o Object to cast to a Double object.
     * @return Representation of the object as Double.
     */
    private static Double getAsDouble(Object o) {
        if (o instanceof Float) {
            return ((Float) o).doubleValue();
        } else if (o instanceof Double) {
            return ((Double) o);
        } else if (o instanceof Integer) {
            return ((Integer) o).doubleValue();
        } else if (o instanceof Long) {
            return ((Long) o).doubleValue();
        }

        throw new IllegalArgumentException("o unknown instanceof. o.getClass():" + o.getClass());
    }

    /**
     * This tries to cast the given object to a Boolean object.
     *
     * @param o Object to cast to a Boolean object.
     * @return Representation of the object as Boolean.
     */
    private static Boolean getAsBoolean(Object o) {
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof Integer) {
            return (Integer) o >= 1;
        } else if (o instanceof String) {
            return ((String) o).toLowerCase().trim().equals("true");
        } else {
            throw new IllegalArgumentException("o is an instance of o.getClass():" + o.getClass());
        }
    }
}
