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

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Restriction;
import org.eclipse.mosaic.lib.database.road.Roundabout;
import org.eclipse.mosaic.lib.database.road.TrafficLightNode;
import org.eclipse.mosaic.lib.database.road.Way;
import org.eclipse.mosaic.lib.database.route.Edge;
import org.eclipse.mosaic.lib.database.route.Route;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.rti.api.MosaicVersion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nonnull;

/**
 * This loads all objects from an SQLite database into the given object database.
 */
public class SQLiteLoader extends SQLiteAccess implements DatabaseLoader {

    /**
     * Holds the current stable version of the database file format.
     * This can be used either to check against when loading a file,
     * or as a substitute for UNKNOWN when saving a database.
     */
    final MosaicVersion stable;

    private final boolean skipVersionCheck;

    /**
     * Default constructor.
     */
    public SQLiteLoader() {
        this(false);
    }

    /**
     * Creates a new {@link SQLiteLoader} object.
     *
     * @param skipVersionCheck Disable the check the version of the database.
     */
    public SQLiteLoader(boolean skipVersionCheck) {
        super();

        this.skipVersionCheck = skipVersionCheck;

        // determine stable version to check against
        try (InputStream propertiesStream = this.getClass().getResourceAsStream("/database.properties")) {
            Properties properties = new Properties();
            properties.load(propertiesStream);
            stable = MosaicVersion.createFromString(properties.getProperty("database.version.stable"));
        } catch (IOException e) {
            log.error("Could not access properties file!");
            throw new IllegalStateException("Could not access properties file while preparing SQLiteLoader!", e);
        }
    }

    /**
     * This loads the given database into our database objects by skipping the values in the list.
     *
     * @param dbFilename Database filename.
     * @return Loaded database.
     * @throws OutdatedDatabaseException if there is an error while reading version from database or the read version is older than stable version.
     */
    public Database.Builder loadFromFile(@Nonnull String dbFilename) throws OutdatedDatabaseException {
        setDatabaseFile(dbFilename);
        log.debug("checking version...");
        Database.Builder builder = new Database.Builder(checkVersion());
        log.debug("Loading properties...");
        loadProperties(builder);
        log.debug("Loading nodes...");
        loadNodes(builder); //Needs to be loaded before any other network related table
        log.debug("Loading ways...");
        loadWays(builder);
        log.debug("Loading way <--> node relations...");
        loadWayNodes(builder);
        log.debug("Loading connections...");
        loadConnections(builder);
        log.debug("Loading connection <--> node relations...");
        loadConnectionNodes(builder);
        log.debug("Loading roundabouts...");
        loadRoundabouts(builder);
        log.debug("Loading restrictions...");
        loadRestrictions(builder);
        log.debug("Loading routes...");
        loadRoutes(builder);
        log.debug("Database loaded!");
        disconnect(null);
        return builder;
    }

    /**
     * Save the database to the filename.
     *
     * @param database Database to save.
     * @param fileName Name of the saved database.
     */
    @Override
    public void saveToFile(@Nonnull Database database, @Nonnull String fileName) {

        // make sure the file has the correct ending
        if (!fileName.endsWith(".db")) {
            fileName += ".db";
        }

        // prepare database file
        setDatabaseFile(fileName);
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
                Statement statement = connect();
                log.debug("setting version and properties...");
                saveProperties(database);
                log.debug("Saving nodes...");
                saveNodes(database);
                log.debug("Saving ways...");
                saveWays(database);
                log.debug("Saving way <--> node relations...");
                saveWayNodes(database);
                log.debug("Saving connections...");
                saveConnections(database);
                log.debug("Saving connection <--> node relations...");
                saveConnectionNodes(database);
                log.debug("Saving restrictions...");
                saveRestrictions(database);
                log.debug("Saving routes...");
                saveRoutes(database);
                log.debug("Saving roundabouts...");
                saveRoundabouts(database);
                log.debug("Database saved!");
                disconnect(statement);
            } catch (SQLException sqle) {
                log.error("error while trying to write database content: {}", sqle.getMessage());
            }
        }
    }

    /**
     * Updates the database from file with the given filename to the latest scheme.
     * Currently no update steps are implemented.
     *
     * @param fileName database filename
     */
    @Override
    public void updateDatabase(String fileName) {
        log.warn(
                "You used \"--update\"  as an option. This is currently not supported and will do nothing. "
                        + "This could change in future versions, for now try to update your database manually or generate it again."
        );
    }

    /**
     * Clears the database of all known/used tables.
     *
     * @throws SQLException Exception that provides information on a database access error or other errors.
     */
    public void clearDb() throws SQLException {
        Statement statement = connect();

        dbConnection.setAutoCommit(false);
        // delete old contents
        statement.addBatch("DROP TABLE IF EXISTS Properties;");
        statement.addBatch("DROP TABLE IF EXISTS Node;");
        statement.addBatch("DROP TABLE IF EXISTS Way;");
        statement.addBatch("DROP TABLE IF EXISTS WayConsistsOf;");
        statement.addBatch("DROP TABLE IF EXISTS Connection;");
        statement.addBatch("DROP TABLE IF EXISTS ConnectionConsistsOf;");
        statement.addBatch("DROP TABLE IF EXISTS ConnectionDetails;");
        statement.addBatch("DROP TABLE IF EXISTS Restriction;");
        statement.addBatch("DROP TABLE IF EXISTS TrafficSignals;");
        statement.addBatch("DROP TABLE IF EXISTS Route;");
        statement.addBatch("DROP TABLE IF EXISTS Corner;");
        statement.addBatch("DROP TABLE IF EXISTS Wall;");
        statement.addBatch("DROP TABLE IF EXISTS Building;");
        statement.addBatch("DROP TABLE IF EXISTS Roundabout;");
        statement.addBatch("DROP TABLE IF EXISTS RoundaboutConsistsOf;");
        statement.executeBatch();
        dbConnection.commit();
        // close connection (includes already a stat.close())
        disconnect(statement);

    }

    /**
     * Creates all necessary tables. TODO: incorporate if not exists or make sure this is ONLY called after {@link #clearDb()}!
     *
     * @throws SQLException Exception that provides information on a database access error or other errors.
     */
    private void initTables() throws SQLException {
        Statement statement = connect();
        // create tables
        // integrity
        statement.executeUpdate("CREATE TABLE Properties (id String UNIQUE, value TEXT)");
        statement.executeUpdate("INSERT INTO Properties(id, value) VALUES ('" + Database.PROPERTY_VERSION + "', '" + Database.VERSION_UNKNOWN + "')");
        statement.executeUpdate("INSERT INTO Properties(id, value) VALUES ('" + Database.PROPERTY_IMPORT_ORIGIN + "', '')");
        // pure network
        statement.executeUpdate("CREATE TABLE Node (id STRING, lat DOUBLE, lon DOUBLE, ele DOUBLE, is_traffic_light BOOLEAN, is_intersection BOOLEAN, is_generated BOOLEAN)");
        statement.executeUpdate("CREATE TABLE Way (id STRING, name TEXT, type TEXT, speed DOUBLE, lanesForward INTEGER, lanesBackward INTEGER, oneway BOOLEAN)");
        statement.executeUpdate("CREATE TABLE WayConsistsOf (way_id STRING, node_id STRING, sequence_number INTEGER)");
        statement.executeUpdate("CREATE TABLE Connection (id STRING, way_id STRING, lanes INTEGER, length FLOAT)");
        statement.executeUpdate("CREATE TABLE ConnectionConsistsOf (connection_id STRING, node_id STRING, sequence_number INTEGER)");
        // turn restrictions
        statement.executeUpdate("CREATE TABLE Restriction (id STRING, source_way_id STRING, via_node_id STRING, target_way_id STRING, type STRING)");
        // traffic signals
        statement.executeUpdate("CREATE TABLE TrafficSignals (id STRING, ref_node_id STRING, phases STRING, timing STRING, from_way_id STRING, via0_way_id STRING, via1_way_id STRING, to_way_id STRING, lanes_from STRING, lanes_via0 STRING, lanes_via1 STRING, lanes_to STRING)");
        // vehicle data
        statement.executeUpdate("CREATE TABLE Route (id STRING, sequence_number INTEGER, connection_id STRING, from_node_id STRING, to_node_id STRING)");
        // buildings
        statement.executeUpdate("CREATE TABLE Corner (id STRING, lat DOUBLE, lon DOUBLE, x DOUBLE, y DOUBLE)");
        statement.executeUpdate("CREATE TABLE Wall (id STRING, building_id STRING, from_corner_id STRING, to_corner_id STRING, length DOUBLE, sequence_number INTEGER)");
        statement.executeUpdate("CREATE TABLE Building (id STRING, name TEXT, height DOUBLE, min_x DOUBLE, max_x DOUBLE, min_y DOUBLE, max_y DOUBLE)");
        // roundabouts
        statement.executeUpdate("CREATE TABLE Roundabout (id STRING)");
        statement.executeUpdate("CREATE TABLE RoundaboutConsistsOf (roundabout_id STRING, node_id STRING, sequence_number INTEGER)");

        // Connection Details (like parking lots)
        statement.executeUpdate("CREATE TABLE ConnectionDetails (id STRING, connection STRING, type STRING, value STRING)");
        disconnect(statement);
    }

    /**
     * Checks the version of the database and creates a database object for usage.
     *
     * @return Database version.
     * @throws OutdatedDatabaseException if there is an error while reading version from database or the read version is older than stable version.
     */
    private String checkVersion() throws OutdatedDatabaseException {
        String versionString;
        try {
            Result queryResult = executeStatement("SELECT value FROM Properties WHERE id = '" + Database.PROPERTY_VERSION + "'");
            versionString = queryResult.getFirstRow().getString("value");
        } catch (SQLException | ArrayIndexOutOfBoundsException e) {
            log.warn("Database follows outdated scheme.", e);
            throw new OutdatedDatabaseException("Database follows outdated scheme.", e);
        }

        // complain about old version if check wasn't skipped
        if (!skipVersionCheck && stable.compareTo(MosaicVersion.createFromString(versionString)) > 0) {
            throw new OutdatedDatabaseException();
        }

        return versionString;
    }

    /**
     * Loads all {@link Node}s from the persistence and writes to the given {@link Database}.
     *
     * @param databaseBuilder Database from which to load.
     */
    private void loadProperties(Database.Builder databaseBuilder) {

        try {
            // get all properties
            List<ResultRow> propertyList = executeStatement(
                    "SELECT id, value FROM Properties"
            ).getRows();

            // rework into objects
            for (ResultRow propertyEntry : propertyList) {

                // read files from entry, mind index order (see columns above)
                String id = propertyEntry.getString("id");
                if (id.equals(Database.PROPERTY_VERSION)) {
                    continue;
                }
                String value = propertyEntry.getString("value", null);
                databaseBuilder.addProperty(id, value);
            }
        } catch (SQLException e) {
            log.warn("Error loading properties: {}. Skipping", e.getMessage());
        }
    }

    /**
     * Loads all {@link Node}s from the persistence and writes to the given {@link Database}. This
     * needs to be loaded before any other network related tables.
     *
     * @param databaseBuilder Database builder from which to load.
     */
    private void loadNodes(Database.Builder databaseBuilder) {

        try {
            // get all nodes
            List<ResultRow> nodeList = executeStatement(
                    "SELECT id, lat, lon, ele, is_traffic_light, is_intersection, is_generated FROM Node"
            ).getRows();

            // rework into objects
            for (ResultRow nodeEntry : nodeList) {

                // read files from entry, mind index order (see columns above)
                String id = nodeEntry.getString("id");
                double latitude = nodeEntry.getDouble("lat");
                double longitude = nodeEntry.getDouble("lon");
                double elevation = nodeEntry.getDouble("ele", 0d);
                boolean isTrafficLight = nodeEntry.getBoolean("is_traffic_light");
                boolean isIntersection = nodeEntry.getBoolean("is_intersection");
                boolean isGenerated = nodeEntry.getBoolean("is_generated");

                // create object and save to db
                Node node = databaseBuilder.addNode(
                        id,
                        GeoPoint.lonLat(longitude, latitude, elevation),
                        isTrafficLight
                );
                node.setIntersection(isIntersection);
                node.setGenerated(isGenerated);
            }
        } catch (SQLException e) {
            log.warn("Error loading nodes: {}. Skipping", e.getMessage());
        }
    }

    /**
     * Loads all {@link Way}s from the persistence and writes to the given {@link Database}. This is
     * the second step when loading the network.
     *
     * @param databaseBuilder Database from which to load.
     */
    private void loadWays(Database.Builder databaseBuilder) {
        try {
            // get all ways
            List<ResultRow> wayList = executeStatement(
                    "SELECT id, name, type, speed, lanesForward, lanesBackward, oneway FROM Way"
            ).getRows();

            for (ResultRow wayEntry : wayList) {

                String id = wayEntry.getString("id");
                String name = wayEntry.getString("name", null);
                String type = wayEntry.getString("type", null);
                double speed = wayEntry.getDouble("speed");
                int lanesForward = wayEntry.getInt("lanesForward");
                int lanesBackward = wayEntry.getInt("lanesBackward");
                boolean oneway = wayEntry.getBoolean("oneway");

                // create object and save to database
                Way way = databaseBuilder.addWay(id, name, type);
                way.setMaxSpeedInMs(speed);
                way.setLanes(lanesForward, lanesBackward);
                way.setIsOneway(oneway);

            }

        } catch (IllegalArgumentException iae) {
            log.error("could not read way from DB please check for consistency");
        } catch (SQLException e) {
            log.warn("Error loading ways: {}. Skipping", e.getMessage());
        }
    }

    /**
     * This loads the relations between {@link Way}s and {@link Node}s.
     *
     * @param databaseBuilder Database from which to load.
     */
    private void loadWayNodes(Database.Builder databaseBuilder) {
        try {
            List<ResultRow> consists = executeStatement(
                    "SELECT way_id, node_id FROM WayConsistsOf ORDER BY sequence_number"
            ).getRows();

            // rework into objects
            for (ResultRow consistsEntry : consists) {
                String wayId = consistsEntry.getString("way_id");
                String nodeId = consistsEntry.getString("node_id");
                databaseBuilder.addNodeToWay(wayId, nodeId);
            }

        } catch (SQLException e) {
            log.warn("Error loading way <--> node relations: {}. Skipping", e.getMessage());
        }
    }

    /**
     * This loads the {@link Connection}s (abstract part of ways that connects junctions).
     *
     * @param databaseBuilder Database from which to load.
     */
    private void loadConnections(Database.Builder databaseBuilder) {
        try {
            List<ResultRow> connections = executeStatement(
                    "SELECT id, way_id, lanes, length FROM Connection"
            ).getRows();

            // rework into objects
            for (ResultRow connectionEntry : connections) {

                String id = connectionEntry.getString("id");
                String wayId = connectionEntry.getString("way_id");
                int lanes = connectionEntry.getInt("lanes");
                double length = connectionEntry.getDouble("length");

                // create object and save to db
                databaseBuilder.addConnection(id, wayId).setLanes(lanes).setLength(length);
            }

        } catch (SQLException e) {
            log.warn("Error loading connections: {}. Skipping", e.getMessage());
        }
    }

    /**
     * This loads the {@link Connection}s and {@link Node}s.
     *
     * @param databaseBuilder Database builder from which to load.
     */
    private void loadConnectionNodes(Database.Builder databaseBuilder) {
        try {
            List<ResultRow> consists = executeStatement(
                    "SELECT connection_id, node_id FROM ConnectionConsistsOf ORDER BY sequence_number"
            ).getRows();

            // rework into objects
            for (ResultRow consistsEntry : consists) {

                // read files from entry, mind index order (see columns above)
                String connectionId = consistsEntry.getString("connection_id");
                String nodeId = consistsEntry.getString("node_id");

                // create object and save to db
                databaseBuilder.addNodeToConnection(connectionId, nodeId);
            }

        } catch (SQLException e) {
            log.warn("Error loading connection <--> node relations: {}. Skipping", e.getMessage());
        }
    }

    /**
     * This loads {@link Roundabout}s.
     *
     * @param databaseBuilder Database builder from which to load.
     */
    private void loadRoundabouts(Database.Builder databaseBuilder) {
        try {
            List<ResultRow> roundabouts = executeStatement(
                    "SELECT id FROM Roundabout"
            ).getRows();

            // rework into objects
            for (ResultRow roundaboutEntry : roundabouts) {

                // read files from entry, mind index order (see columns above)
                String id = roundaboutEntry.getString("id");

                // create object and save to db
                List<Node> nodes = loadRoundaboutNodes(id, databaseBuilder);
                databaseBuilder.addRoundabout(id, nodes);
            }
        } catch (Exception e) {
            log.warn("No roundabouts were found in the scenario database. Note that roundabouts in this scenario may be treated as ordinary crossings.");
        }
    }

    /**
     * This loads {@link Node}-References (IDs) that are part of the {@link Roundabout} with id `roundaboutId`.
     *
     * @param roundaboutId Id of the roundabout from which to load the nodes.
     */
    private List<Node> loadRoundaboutNodes(String roundaboutId, Database.Builder databaseBuilder) {
        try {
            List<ResultRow> restrictions = executeStatement(
                    "SELECT node_id FROM RoundaboutConsistsOf WHERE roundabout_id = \""
                            + roundaboutId + "\" ORDER BY sequence_number ASC"
            ).getRows();
            List<Node> restrictionNodes = new ArrayList<>();
            for (ResultRow restriction : restrictions) {
                restrictionNodes.add(databaseBuilder.getNode(restriction.getString("id")));
            }
            return restrictionNodes;
        } catch (Exception e) {
            log.warn("Error loading roundabout nodes: {}. Skipping", e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * This loads the turn restrictions on {@link Connection}s via a {@link Node}.
     *
     * @param databaseBuilder Database builder from which to load the restrictions.
     */
    private void loadRestrictions(Database.Builder databaseBuilder) {
        databaseBuilder.completeConnections();

        try {
            List<ResultRow> restrictions = executeStatement(
                    "SELECT id, source_way_id, via_node_id, target_way_id, type FROM Restriction"
            ).getRows();

            for (ResultRow restrictionEntry : restrictions) {
                String restrictionId = restrictionEntry.getString("id");
                String sourceWayId = restrictionEntry.getString("source_way_id");
                String viaNodeId = restrictionEntry.getString("via_node_id");
                String targetWayId = restrictionEntry.getString("target_way_id");
                Restriction.Type type = Restriction.Type.convertTypeFromString(restrictionEntry.getString("type"));

                databaseBuilder.addRestriction(restrictionId, type, sourceWayId, viaNodeId, targetWayId);
            }

        } catch (SQLException e) {
            log.warn("Error loading restrictions: {}, Skipping", e.getMessage());
        }
    }

    /**
     * This loads the {@link Route}s.
     *
     * @param databaseBuilder Database from which to load the routes.
     */
    private void loadRoutes(Database.Builder databaseBuilder) {
        try {
            List<ResultRow> routes = executeStatement(
                    "SELECT id, connection_id, from_node_id, to_node_id FROM Route ORDER BY id, sequence_number"
            ).getRows();

            String lastId = null;
            Database.RouteBuilder routeBuilder = null;

            // rework into objects
            for (ResultRow routeEntry : routes) {

                // read files from entry, mind index order (see columns above)
                String id = routeEntry.getString("id");
                String connectionId = routeEntry.getString("connection_id");
                String fromNodeId = routeEntry.getString("from_node_id");
                String toNodeId = routeEntry.getString("to_node_id");

                // we need to group into our route object
                if (!id.equals(lastId)) {
                    if (routeBuilder != null) {
                        routeBuilder.create();
                    }
                    routeBuilder = databaseBuilder.addRoute(id);
                    lastId = id;
                }
                routeBuilder.addEdge(connectionId, fromNodeId, toNodeId);
            }

            if (routeBuilder != null) {
                routeBuilder.create();
            }
        } catch (SQLException e) {
            log.warn("Error loading routes: {}. Skipping", e.getMessage());
        }
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
            String statement = "UPDATE Properties SET value = ? WHERE id = ?";
            try (PreparedStatement prep = dbConnection.prepareStatement(statement)) {
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
        String statement = "INSERT INTO Node(" + columns + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement prep = dbConnection.prepareStatement(statement)) {
            boolean autoCommit = dbConnection.getAutoCommit();
            dbConnection.setAutoCommit(false);
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
            dbConnection.commit();
            dbConnection.setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist nodes: {}", sqle.getMessage());
        }
    }

    /**
     * Saves all {@link Way}s to the SQLite file. Be aware that references(/relations) to other
     * objects are saved separately!
     *
     * @param database Save ways into the database.
     */
    private void saveWays(Database database) {
        String columns = "id, name, type, speed, lanesForward, lanesBackward, oneway";
        String statement = "INSERT INTO Way(" + columns + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement prep = dbConnection.prepareStatement(statement)) {
            boolean autoCommit = dbConnection.getAutoCommit();
            dbConnection.setAutoCommit(false);
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
            dbConnection.commit();
            dbConnection.setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist ways: {}", sqle.getMessage());
        }
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
            final String newRoundaboutStatement = "INSERT INTO Roundabout(id) VALUES (?)";
            try (PreparedStatement prep = dbConnection.prepareStatement(newRoundaboutStatement)) {
                final boolean autoCommit = dbConnection.getAutoCommit();
                dbConnection.setAutoCommit(false);
                prep.setString(1, roundabout.getId());
                prep.executeUpdate();
                dbConnection.commit();
                dbConnection.setAutoCommit(autoCommit);
            } catch (SQLException sqle) {
                log.error("error while trying to persist roundabouts: {}", sqle.getMessage());
            }

            // Add roundabout node references
            final String columns = "roundabout_id, node_id, sequence_number";
            final String statement = "INSERT INTO RoundaboutConsistsOf(" + columns + ") VALUES (?, ?, ?)";
            int sequenceNumber = 0;
            try (PreparedStatement prep = dbConnection.prepareStatement(statement)) {
                final boolean autoCommit = dbConnection.getAutoCommit();
                dbConnection.setAutoCommit(false);
                for (Node node : roundabout.getNodes()) {
                    prep.setString(1, roundabout.getId());
                    prep.setString(2, node.getId());
                    prep.setInt(3, sequenceNumber);
                    prep.executeUpdate();
                    sequenceNumber++;
                }
                dbConnection.commit();
                dbConnection.setAutoCommit(autoCommit);
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
        String statement = "INSERT INTO WayConsistsOf(" + columns + ") VALUES (?, ?, ?)";
        int sequenceNumber;
        try (PreparedStatement prep = dbConnection.prepareStatement(statement)) {
            boolean autoCommit = dbConnection.getAutoCommit();
            dbConnection.setAutoCommit(false);
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
            dbConnection.commit();
            dbConnection.setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist ways <--> node relations: {}",
                    sqle.getMessage());
        }
    }

    /**
     * Saves all {@link Connection}s to the SQLite file. Be aware that references(/relations) to
     * other objects are saved separately!
     *
     * @param database Save connections into the database.
     */
    private void saveConnections(Database database) {
        String columns = "id, way_id, lanes, length";
        String statement = "INSERT INTO Connection(" + columns + ") VALUES (?, ?, ?, ?)";
        try (PreparedStatement prep = dbConnection.prepareStatement(statement)) {
            boolean autoCommit = dbConnection.getAutoCommit();
            dbConnection.setAutoCommit(false);
            for (Connection connection : database.getConnections()) {
                prep.setString(1, connection.getId());
                prep.setString(2, connection.getWay().getId());
                prep.setInt(3, connection.getLanes());
                prep.setDouble(4, connection.getLength());
                prep.executeUpdate();
            }
            dbConnection.commit();
            dbConnection.setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist connections: {}", sqle.getMessage());
        }
    }

    /**
     * Saves all references/relations between {@link Connection}s and {@link Node}s to the SQLite
     * file.
     *
     * @param database Save nodes of the connection into the database.
     */
    private void saveConnectionNodes(Database database) {
        String columns = "connection_id, node_id, sequence_number";
        String statement = "INSERT INTO ConnectionConsistsOf(" + columns + ") VALUES (?, ?, ?)";
        int sequenceNumber;
        try (PreparedStatement prep = dbConnection.prepareStatement(statement)) {
            boolean autoCommit = dbConnection.getAutoCommit();
            dbConnection.setAutoCommit(false);
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
            dbConnection.commit();
            dbConnection.setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist connection <--> node relations: {}",
                    sqle.getMessage());
        }
    }

    /**
     * Save all {@link Restriction}s to the SQLite file.
     *
     * @param database Save restrictions into the database.
     */
    private void saveRestrictions(Database database) {
        String columns = "id, source_way_id, via_node_id, target_way_id, type";
        String statement = "INSERT INTO Restriction(" + columns + ") VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement prep = dbConnection.prepareStatement(statement)) {
            boolean autoCommit = dbConnection.getAutoCommit();
            dbConnection.setAutoCommit(false);
            for (Restriction restriction : database.getRestrictions()) {
                prep.setString(1, restriction.getId());
                prep.setString(2, restriction.getSource().getId());
                prep.setString(3, restriction.getVia().getId());
                prep.setString(4, restriction.getTarget().getId());
                prep.setString(5, restriction.getType().name());
                prep.executeUpdate();
            }
            dbConnection.commit();
            dbConnection.setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist restriction; {}", sqle.getMessage());
        }
    }

    /**
     * Saves all {@link Route}s to the SQLite file.
     *
     * @param database Save routes into the database.
     */
    private void saveRoutes(Database database) {
        String columns = "id, sequence_number, connection_id, from_node_id, to_node_id";
        String statement = "INSERT INTO Route(" + columns + ") VALUES (?, ?, ?, ?, ?)";
        int sequenceNumber;
        try (PreparedStatement prep = dbConnection.prepareStatement(statement)) {
            boolean autoCommit = dbConnection.getAutoCommit();
            dbConnection.setAutoCommit(false);
            for (Route route : database.getRoutes()) {
                sequenceNumber = 0;
                for (Edge edge : route.getEdges()) {
                    prep.setString(1, route.getId());
                    prep.setInt(2, sequenceNumber);
                    prep.setString(3, edge.getConnection().getId());
                    prep.setString(4, edge.getFromNode().getId());
                    prep.setString(5, edge.getToNode().getId());

                    prep.executeUpdate();
                    sequenceNumber++;
                }
            }
            dbConnection.commit();
            dbConnection.setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            log.error("error while trying to persist routes: {}", sqle.getMessage());
        }
    }

}
