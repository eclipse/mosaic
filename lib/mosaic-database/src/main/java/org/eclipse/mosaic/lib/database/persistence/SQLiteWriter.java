/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.building.Building;
import org.eclipse.mosaic.lib.database.building.Wall;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Restriction;
import org.eclipse.mosaic.lib.database.road.Roundabout;
import org.eclipse.mosaic.lib.database.road.TrafficLightNode;
import org.eclipse.mosaic.lib.database.road.Way;
import org.eclipse.mosaic.lib.database.route.Route;
import org.eclipse.mosaic.rti.api.MosaicVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.annotation.Nonnull;

/**
 * This stores all objects from the given object database to a persistent SQLite database.
 */
public class SQLiteWriter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final SQLiteAccess sqlite = new SQLiteAccess();

    /**
     * Holds the current stable version of the database file format.
     * This can be used either to check against when loading a file,
     * or as a substitute for UNKNOWN when saving a database.
     */
    final MosaicVersion stable;

    /**
     * Constructor for the {@link SQLiteWriter} reading the properties to retrieve the stable database version.
     */
    public SQLiteWriter() {
        // determine stable version to check against
        try (InputStream propertiesStream = this.getClass().getResourceAsStream("/database.properties")) {
            Properties properties = new Properties();
            properties.load(propertiesStream);
            stable = MosaicVersion.createFromString(properties.getProperty("database.version.stable"));
        } catch (IOException e) {
            log.error("Could not access properties file.");
            throw new IllegalStateException("Could not access properties file while preparing SQLiteWriter.", e);
        }
    }

    /**
     * Save the database to the filename.
     *
     * @param database Database to save.
     * @param fileName Name of the saved database.
     */
    public void saveToFile(@Nonnull Database database, @Nonnull String fileName) {

        // make sure the file has the correct ending
        if (!fileName.endsWith(".db")) {
            fileName += ".db";
        }

        // prepare database file
        sqlite.setDatabaseFile(fileName);
        boolean goSave = false;
        try {
            clearDb();
            initTables();
            goSave = true;
        } catch (SQLException sqle) {
            log.error("could not initialize database for saving: {}", sqle.getMessage());
        }

        // now actually write data
        if (goSave) {
            try {
                Statement statement = sqlite.connect();
                executeSaves(database);
                sqlite.disconnect(statement);
            } catch (SQLException sqle) {
                log.error("error while trying to write database content: {}", sqle.getMessage());
            }
        }
    }

    private void executeSaves(@Nonnull Database database) throws SQLException {
        log.debug("setting version and properties...");
        saveProperties(database);
        log.info("Saving {} nodes...", database.getNodes().size());
        saveNodes(database);
        log.info("Saving {} ways...", database.getWays().size());
        saveWays(database);
        saveWayNodes(database);
        log.info("Saving {} connections...", database.getConnections().size());
        saveConnections(database);
        saveConnectionNodes(database);
        log.info("Saving {} restrictions...", database.getRestrictions().size());
        saveRestrictions(database);
        log.info("Saving {} routes...", database.getRoutes().size());
        saveRoutes(database);
        log.info("Saving {} roundabouts...", database.getRoundabouts().size());
        saveRoundabouts(database);
        log.info("Saving {} buildings...", database.getBuildings().size());
        saveBuildings(database);
        log.info("Create Indices");
        createIndices();
        log.info("Database saved");
    }


    /**
     * Clears the database of all known/used tables.
     *
     * @throws SQLException Exception that provides information on a database access error or other errors.
     */
    public void clearDb() throws SQLException {
        Statement statement = sqlite.connect();

        sqlite.getConnection().setAutoCommit(false);
        // delete old contents
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.PROPERTIES + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.NODE + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.WAY + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.WAY_CONSISTS_OF + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.CONNECTION + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.CONNECTION_CONSISTS_OF + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.CONNECTION_DETAILS + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.RESTRICTION + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.TRAFFIC_SIGNALS + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.ROUTE + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.BUILDING + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.BUILDING_CONSISTS_OF + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.ROUNDABOUT + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.ROUNDABOUT_CONSISTS_OF + ";");
        // removing deprecated tables
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.CORNER + ";");
        statement.addBatch("DROP TABLE IF EXISTS " + TABLES.WALL + ";");
        statement.executeBatch();
        sqlite.getConnection().commit();
        // close connection (includes already a stat.close())
        sqlite.disconnect(statement);
    }

    /**
     * Creates all necessary tables.
     *
     * @throws SQLException Exception that provides information on a database access error or other errors.
     */
    private void initTables() throws SQLException {
        Statement statement = sqlite.connect();
        // create tables
        // integrity
        statement.executeUpdate("CREATE TABLE " + TABLES.PROPERTIES + " (id TEXT UNIQUE, value TEXT)");
        statement.executeUpdate("INSERT INTO " + TABLES.PROPERTIES
                + "(id, value) VALUES ('" + Database.PROPERTY_VERSION + "', '" + Database.VERSION_UNKNOWN + "')");
        statement.executeUpdate("INSERT INTO " + TABLES.PROPERTIES
                + "(id, value) VALUES ('" + Database.PROPERTY_IMPORT_ORIGIN + "', '')");
        // pure network
        statement.executeUpdate("CREATE TABLE " + TABLES.NODE
                + " (id TEXT, lat REAL, lon REAL, ele REAL, is_traffic_light BOOLEAN, is_intersection BOOLEAN, is_generated BOOLEAN)");
        statement.executeUpdate("CREATE TABLE "
                + TABLES.WAY + " (id TEXT, name TEXT, type TEXT, speed REAL, lanesForward INTEGER, lanesBackward INTEGER, oneway BOOLEAN)");
        statement.executeUpdate("CREATE TABLE "
                + TABLES.WAY_CONSISTS_OF + " (way_id TEXT, node_id TEXT, sequence_number INTEGER)");
        statement.executeUpdate("CREATE TABLE "
                + TABLES.CONNECTION + " (id TEXT, way_id TEXT, lanes INTEGER, length REAL)");
        statement.executeUpdate("CREATE TABLE "
                + TABLES.CONNECTION_CONSISTS_OF + " (connection_id TEXT, node_id TEXT, sequence_number INTEGER)");
        // turn restrictions
        statement.executeUpdate("CREATE TABLE " + TABLES.RESTRICTION
                + " (id TEXT, source_way_id TEXT, via_node_id TEXT, target_way_id TEXT, type TEXT)");
        // traffic signals
        statement.executeUpdate("CREATE TABLE " + TABLES.TRAFFIC_SIGNALS
                + " (id TEXT, ref_node_id TEXT, phases TEXT, timing TEXT, from_way_id TEXT, via0_way_id TEXT, via1_way_id TEXT,"
                + " to_way_id TEXT, lanes_from TEXT, lanes_via0 TEXT, lanes_via1 TEXT, lanes_to TEXT)");
        // vehicle data
        statement.executeUpdate("CREATE TABLE " + TABLES.ROUTE
                + " (id TEXT, sequence_number INTEGER, connection_id TEXT)");
        // roundabouts
        statement.executeUpdate("CREATE TABLE " + TABLES.ROUNDABOUT + " (id TEXT)");
        statement.executeUpdate("CREATE TABLE "
                + TABLES.ROUNDABOUT_CONSISTS_OF + " (roundabout_id TEXT, node_id TEXT, sequence_number INTEGER)");
        // buildings and its corners
        statement.executeUpdate("CREATE TABLE " + TABLES.BUILDING + " (id TEXT, name TEXT, height REAL)");
        statement.executeUpdate("CREATE TABLE "
                + TABLES.BUILDING_CONSISTS_OF + " (building_id TEXT, lat REAL, lon REAL, sequence_number INTEGER)");

        // Connection Details (like parking lots)
        statement.executeUpdate("CREATE TABLE " + TABLES.CONNECTION_DETAILS + " (id TEXT, connection TEXT, type TEXT, value TEXT)");
        sqlite.disconnect(statement);
    }

    /**
     * Creates indices for faster queries when using WHERE clause.
     *
     * @throws SQLException Exception that provides information on a database access error or other errors.
     */
    private void createIndices() throws SQLException {
        Statement statement = sqlite.connect();
        statement.executeUpdate("CREATE INDEX building_index on " + TABLES.BUILDING_CONSISTS_OF + "(building_id)");
        statement.executeUpdate("CREATE INDEX roundabout_index on " + TABLES.ROUNDABOUT_CONSISTS_OF + "(roundabout_id)");
        sqlite.disconnect(statement);
    }

    /**
     * Saves all other properties beyond the version in the database.
     *
     * @param database Save properties into the database.
     */
    private void saveProperties(Database database) {
        for (String currentKey : database.getPropertyKeys()) {
            String currentValue = database.getProperty(currentKey);
            if (currentKey.equals(Database.PROPERTY_VERSION)) {
                // As "UNKNOWN" is neither correct nor can be loaded, we overwrite with stable.
                if (currentValue.equals(Database.VERSION_UNKNOWN)) {
                    currentValue = stable.toString();
                }
            }
            // actually write away
            String statement = "UPDATE " + TABLES.PROPERTIES + " SET value = ? WHERE id = ?";
            try (PreparedStatement prep = sqlite.getConnection().prepareStatement(statement)) {
                prep.setString(1, currentValue);
                prep.setString(2, currentKey);
                prep.executeUpdate();
            } catch (SQLException sqle) {
                log.error("error while trying to persist property: {}", sqle.getMessage());
            }
        }
    }

    /**
     * Saves all {@link Node}s to the SQLite file. Be aware that references(/relations) to other
     * objects are saved separately!
     *
     * @param database Save nodes into the database.
     */
    private void saveNodes(Database database) {
        String columns = "id, lon, lat, ele, is_traffic_light, is_intersection, is_generated";
        String statement = "INSERT INTO " + TABLES.NODE + "(" + columns + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement prep = sqlite.getConnection().prepareStatement(statement)) {
            boolean autoCommit = sqlite.getConnection().getAutoCommit();
            batchSaveNodes(database, prep);
            sqlite.getConnection().setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist nodes: {}", sqle.getMessage());
        }
    }

    private void batchSaveNodes(Database database, PreparedStatement prep) throws SQLException {
        sqlite.getConnection().setAutoCommit(false);
        for (Node node : database.getNodes()) {
            prep.setString(1, node.getId());
            prep.setDouble(2, node.getPosition().getLongitude());
            prep.setDouble(3, node.getPosition().getLatitude());
            prep.setDouble(4, node.getPosition().getAltitude());
            prep.setBoolean(5, node.getClass().equals(TrafficLightNode.class));
            prep.setBoolean(6, node.isIntersection());
            prep.setBoolean(7, node.isGenerated());
            prep.executeUpdate();
        }
        sqlite.getConnection().commit();
    }

    /**
     * Saves all {@link Way}s to the SQLite file. Be aware that references(/relations) to other
     * objects are saved separately!
     *
     * @param database Save ways into the database.
     */
    private void saveWays(Database database) {
        String columns = "id, name, type, speed, lanesForward, lanesBackward, oneway";
        String statement = "INSERT INTO " + TABLES.WAY + "(" + columns + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement prep = sqlite.getConnection().prepareStatement(statement)) {
            boolean autoCommit = sqlite.getConnection().getAutoCommit();
            batchSaveWays(database, prep);
            sqlite.getConnection().setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist ways: {}", sqle.getMessage());
        }
    }

    private void batchSaveWays(Database database, PreparedStatement prep) throws SQLException {
        sqlite.getConnection().setAutoCommit(false);
        for (Way way : database.getWays()) {
            prep.setString(1, way.getId());
            prep.setString(2, way.getName());
            prep.setString(3, way.getType());
            prep.setDouble(4, way.getMaxSpeedInMs());
            prep.setInt(5, way.getNumberOfLanesForward());
            prep.setInt(6, way.getNumberOfLanesBackward());
            prep.setBoolean(7, way.isOneway());
            prep.executeUpdate();
        }
        sqlite.getConnection().commit();
    }

    /**
     * Saves all {@link Roundabout}s to the SQLite file. References(/relations) to other
     * objects NOT are saved separately!
     *
     * @param database Save roundabouts into the database.
     */
    private void saveRoundabouts(Database database) {
        for (Roundabout roundabout : database.getRoundabouts()) {

            // Create new roundabout reference in database
            final String newRoundaboutStatement = "INSERT INTO " + TABLES.ROUNDABOUT + "(id) VALUES (?)";
            try (PreparedStatement prep = sqlite.getConnection().prepareStatement(newRoundaboutStatement)) {
                final boolean autoCommit = sqlite.getConnection().getAutoCommit();
                sqlite.getConnection().setAutoCommit(false);
                prep.setString(1, roundabout.getId());
                prep.executeUpdate();
                sqlite.getConnection().commit();
                sqlite.getConnection().setAutoCommit(autoCommit);
            } catch (SQLException sqle) {
                log.error("error while trying to persist roundabouts: {}", sqle.getMessage());
            }

            // Add roundabout node references
            final String columns = "roundabout_id, node_id, sequence_number";
            final String statement = "INSERT INTO " + TABLES.ROUNDABOUT_CONSISTS_OF + "(" + columns + ") VALUES (?, ?, ?)";
            int sequenceNumber = 0;
            try (PreparedStatement prep = sqlite.getConnection().prepareStatement(statement)) {
                final boolean autoCommit = sqlite.getConnection().getAutoCommit();
                sqlite.getConnection().setAutoCommit(false);
                for (Node node : roundabout.getNodes()) {
                    prep.setString(1, roundabout.getId());
                    prep.setString(2, node.getId());
                    prep.setInt(3, sequenceNumber);
                    prep.executeUpdate();
                    sequenceNumber++;
                }
                sqlite.getConnection().commit();
                sqlite.getConnection().setAutoCommit(autoCommit);
            } catch (SQLException sqle) {
                log.error("error while trying to persist roundabouts <--> node relations: {}",
                        sqle.getMessage());
            }
        }
    }

    /**
     * Saves all references/relations between {@link Way}s and {@link Node}s to the SQLite file.
     *
     * @param database Save nodes of the way into the database.
     */
    private void saveWayNodes(Database database) {
        String columns = "way_id, node_id, sequence_number";
        String statement = "INSERT INTO " + TABLES.WAY_CONSISTS_OF + "(" + columns + ") VALUES (?, ?, ?)";
        try (PreparedStatement prep = sqlite.getConnection().prepareStatement(statement)) {
            boolean autoCommit = sqlite.getConnection().getAutoCommit();
            batchSaveWayNodes(database, prep);
            sqlite.getConnection().setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist ways <--> node relations: {}",
                    sqle.getMessage());
        }
    }

    private void batchSaveWayNodes(Database database, PreparedStatement prep) throws SQLException {
        int sequenceNumber;
        sqlite.getConnection().setAutoCommit(false);
        for (Way way : database.getWays()) {
            sequenceNumber = 0;
            for (Node node : way.getNodes()) {
                prep.setString(1, way.getId());
                prep.setString(2, node.getId());
                prep.setInt(3, sequenceNumber);
                prep.executeUpdate();
                sequenceNumber++;
            }
        }
        sqlite.getConnection().commit();
    }

    /**
     * Saves all {@link Connection}s to the SQLite file. Be aware that references(/relations) to
     * other objects are saved separately!
     *
     * @param database Save connections into the database.
     */
    private void saveConnections(Database database) {
        String columns = "id, way_id, lanes, length";
        String statement = "INSERT INTO " + TABLES.CONNECTION + "(" + columns + ") VALUES (?, ?, ?, ?)";
        try (PreparedStatement prep = sqlite.getConnection().prepareStatement(statement)) {
            boolean autoCommit = sqlite.getConnection().getAutoCommit();
            batchSaveConnections(database, prep);
            sqlite.getConnection().setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist connections: {}", sqle.getMessage());
        }
    }

    private void batchSaveConnections(Database database, PreparedStatement prep) throws SQLException {
        sqlite.getConnection().setAutoCommit(false);
        for (Connection connection : database.getConnections()) {
            prep.setString(1, connection.getId());
            prep.setString(2, connection.getWay().getId());
            prep.setInt(3, connection.getLanes());
            prep.setDouble(4, connection.getLength());
            prep.executeUpdate();
        }
        sqlite.getConnection().commit();
    }

    /**
     * Saves all references/relations between {@link Connection}s and {@link Node}s to the SQLite
     * file.
     *
     * @param database Save nodes of the connection into the database.
     */
    private void saveConnectionNodes(Database database) {
        String columns = "connection_id, node_id, sequence_number";
        String statement = "INSERT INTO " + TABLES.CONNECTION_CONSISTS_OF + "(" + columns + ") VALUES (?, ?, ?)";
        try (PreparedStatement prep = sqlite.getConnection().prepareStatement(statement)) {
            boolean autoCommit = sqlite.getConnection().getAutoCommit();
            batchSaveConnectionNoes(database, prep);
            sqlite.getConnection().setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist connection <--> node relations: {}",
                    sqle.getMessage());
        }
    }

    private void batchSaveConnectionNoes(Database database, PreparedStatement prep) throws SQLException {
        int sequenceNumber;
        sqlite.getConnection().setAutoCommit(false);
        for (Connection connection : database.getConnections()) {
            sequenceNumber = 0;
            for (Node node : connection.getNodes()) {
                prep.setString(1, connection.getId());
                prep.setString(2, node.getId());
                prep.setInt(3, sequenceNumber);
                prep.executeUpdate();
                sequenceNumber++;
            }
        }
        sqlite.getConnection().commit();
    }

    /**
     * Save all {@link Restriction}s to the SQLite file.
     *
     * @param database Save restrictions into the database.
     */
    private void saveRestrictions(Database database) {
        String columns = "id, source_way_id, via_node_id, target_way_id, type";
        String statement = "INSERT INTO " + TABLES.RESTRICTION + "(" + columns + ") VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement prep = sqlite.getConnection().prepareStatement(statement)) {
            boolean autoCommit = sqlite.getConnection().getAutoCommit();
            batchSaveRestrictions(database, prep);
            sqlite.getConnection().setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist restriction; {}", sqle.getMessage());
        }
    }

    private void batchSaveRestrictions(Database database, PreparedStatement prep) throws SQLException {
        sqlite.getConnection().setAutoCommit(false);
        for (Restriction restriction : database.getRestrictions()) {
            prep.setString(1, restriction.getId());
            prep.setString(2, restriction.getSource().getId());
            prep.setString(3, restriction.getVia().getId());
            prep.setString(4, restriction.getTarget().getId());
            prep.setString(5, restriction.getType().name());
            prep.executeUpdate();
        }
        sqlite.getConnection().commit();
    }

    /**
     * Saves all {@link Building}s to the SQLite file. Basic attributes are stored in the table `Building`.
     * Furthermore, for each building, a list of corner points are stored in the table `BuildingConsistsOf`.
     *
     * @param database Save roundabouts into the database.
     */
    private void saveBuildings(Database database) {

        final String buildingStatement = "INSERT INTO " + TABLES.BUILDING + "(id, name, height) VALUES (?, ?, ?)";

        // Create new building in database
        try (PreparedStatement prep = sqlite.getConnection().prepareStatement(buildingStatement)) {
            final boolean autoCommit = sqlite.getConnection().getAutoCommit();
            sqlite.getConnection().setAutoCommit(false);
            for (Building building : database.getBuildings()) {
                prep.setString(1, building.getId());
                prep.setString(2, building.getName());
                prep.setDouble(3, building.getHeight());
                prep.executeUpdate();

            }
            sqlite.getConnection().commit();
            sqlite.getConnection().setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist buildings: {}", sqle.getMessage());
        }

        // Store corner list of building in database
        final String cornerStatement = "INSERT INTO " + TABLES.BUILDING_CONSISTS_OF
                + "(building_id, lat, lon, sequence_number) VALUES (?, ?, ?, ?)";

        try (PreparedStatement prep = sqlite.getConnection().prepareStatement(cornerStatement)) {
            final boolean autoCommit = sqlite.getConnection().getAutoCommit();
            sqlite.getConnection().setAutoCommit(false);
            for (Building building : database.getBuildings()) {
                int sequenceNumber = 0;
                for (Wall wall : building.getWalls()) {
                    prep.setString(1, building.getId());
                    // we only store the from-node of each wall, as we already can expect a closed loop of walls
                    prep.setDouble(2, wall.getFromCorner().getPosition().getLatitude());
                    prep.setDouble(3, wall.getFromCorner().getPosition().getLongitude());
                    prep.setInt(4, sequenceNumber++);
                    prep.executeUpdate();
                }
            }
            sqlite.getConnection().commit();
            sqlite.getConnection().setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist building corners: {}", sqle.getMessage());
        }

    }

    /**
     * Saves all {@link Route}s to the SQLite file.
     *
     * @param database Save routes into the database.
     */
    private void saveRoutes(Database database) {
        String columns = "id, sequence_number, connection_id";
        String statement = "INSERT INTO " + TABLES.ROUTE + "(" + columns + ") VALUES (?, ?, ?)";
        try (PreparedStatement prep = sqlite.getConnection().prepareStatement(statement)) {
            boolean autoCommit = sqlite.getConnection().getAutoCommit();
            batchSaveRoutes(database, prep);
            sqlite.getConnection().setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist routes: {}", sqle.getMessage());
        }
    }

    private void batchSaveRoutes(Database database, PreparedStatement prep) throws SQLException {
        int sequenceNumber;
        sqlite.getConnection().setAutoCommit(false);
        for (Route route : database.getRoutes()) {
            sequenceNumber = 0;
            for (Connection connection : route.getConnections()) {
                prep.setString(1, route.getId());
                prep.setInt(2, sequenceNumber);
                prep.setString(3, connection.getId());

                prep.executeUpdate();
                sequenceNumber++;
            }
        }
        sqlite.getConnection().commit();
    }

}
