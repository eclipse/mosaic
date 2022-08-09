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
import org.eclipse.mosaic.lib.database.building.Building;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Restriction;
import org.eclipse.mosaic.lib.database.road.Roundabout;
import org.eclipse.mosaic.lib.database.road.Way;
import org.eclipse.mosaic.lib.database.route.Route;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.rti.api.MosaicVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nonnull;

/**
 * This reads all objects from an SQLite database into the given object database.
 */
public class SQLiteReader implements DatabaseLoader {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final SQLiteAccess sqlite = new SQLiteAccess();

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
    public SQLiteReader() {
        this(false);
    }

    /**
     * Creates a new {@link SQLiteReader} object.
     *
     * @param skipVersionCheck Disable the check the version of the database.
     */
    public SQLiteReader(boolean skipVersionCheck) {
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
        sqlite.setDatabaseFile(dbFilename);
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
        log.debug("Loading buildings...");
        loadBuildings(builder);
        log.debug("Loading routes...");
        loadRoutes(builder);
        log.debug("Database loaded");
        sqlite.disconnect(null);
        return builder;
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
            SQLiteAccess.Result queryResult = sqlite.executeStatement("SELECT value FROM Properties WHERE id = '" + Database.PROPERTY_VERSION + "'");
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
            List<SQLiteAccess.ResultRow> propertyList = sqlite.executeStatement(
                    "SELECT id, value FROM Properties"
            ).getRows();

            // rework into objects
            for (SQLiteAccess.ResultRow propertyEntry : propertyList) {

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
            List<SQLiteAccess.ResultRow> nodeList = sqlite.executeStatement(
                    "SELECT id, lat, lon, ele, is_traffic_light, is_intersection, is_generated FROM Node"
            ).getRows();

            // rework into objects
            for (SQLiteAccess.ResultRow nodeEntry : nodeList) {

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
            List<SQLiteAccess.ResultRow> wayList = sqlite.executeStatement(
                    "SELECT id, name, type, speed, lanesForward, lanesBackward, oneway FROM Way"
            ).getRows();

            for (SQLiteAccess.ResultRow wayEntry : wayList) {

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
            List<SQLiteAccess.ResultRow> consists = sqlite.executeStatement(
                    "SELECT way_id, node_id FROM WayConsistsOf ORDER BY sequence_number"
            ).getRows();

            // rework into objects
            for (SQLiteAccess.ResultRow consistsEntry : consists) {
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
            List<SQLiteAccess.ResultRow> connections = sqlite.executeStatement(
                    "SELECT id, way_id, lanes, length FROM Connection"
            ).getRows();

            // rework into objects
            for (SQLiteAccess.ResultRow connectionEntry : connections) {

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
            List<SQLiteAccess.ResultRow> consists = sqlite.executeStatement(
                    "SELECT connection_id, node_id FROM ConnectionConsistsOf ORDER BY sequence_number"
            ).getRows();

            // rework into objects
            for (SQLiteAccess.ResultRow consistsEntry : consists) {

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
            List<SQLiteAccess.ResultRow> roundabouts = sqlite.executeStatement(
                    "SELECT id FROM Roundabout"
            ).getRows();

            // rework into objects
            for (SQLiteAccess.ResultRow roundaboutEntry : roundabouts) {

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
            List<SQLiteAccess.ResultRow> restrictions = sqlite.executeStatement(
                    "SELECT node_id FROM RoundaboutConsistsOf WHERE roundabout_id = \""
                            + roundaboutId + "\" ORDER BY sequence_number ASC"
            ).getRows();
            List<Node> restrictionNodes = new ArrayList<>();
            for (SQLiteAccess.ResultRow restriction : restrictions) {
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
            List<SQLiteAccess.ResultRow> restrictions = sqlite.executeStatement(
                    "SELECT id, source_way_id, via_node_id, target_way_id, type FROM Restriction"
            ).getRows();

            for (SQLiteAccess.ResultRow restrictionEntry : restrictions) {
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
     * This loads the {@link Building}s from the database.
     *
     * @param databaseBuilder Database from which to load the buildings.
     */
    private void loadBuildings(Database.Builder databaseBuilder) {
        try {
            List<SQLiteAccess.ResultRow> buildingEntries = sqlite.executeStatement("SELECT id, name, height FROM Building").getRows();

            // rework into objects
            for (SQLiteAccess.ResultRow buildingEntry : buildingEntries) {
                String id = buildingEntry.getString("id");
                String name = buildingEntry.getString("name");
                double height = buildingEntry.getDouble("height");

                List<SQLiteAccess.ResultRow> cornerEntries = sqlite.executeStatement(
                        "SELECT lat, lon FROM BuildingConsistsOf WHERE building_id = \"" + id + "\" ORDER BY sequence_number"
                ).getRows();

                final GeoPoint[] corners = new GeoPoint[cornerEntries.size()];
                int i = 0;
                for (SQLiteAccess.ResultRow cornerEntry : cornerEntries) {
                    corners[i++] = GeoPoint.latLon(cornerEntry.getDouble("lat"), cornerEntry.getDouble("lon"));
                }

                // create building and save to db
                databaseBuilder.addBuilding(id, name, height, corners);
            }

        } catch (SQLException e) {
            log.warn("Error loading buildings: {}. Skipping", e.getMessage());
        }
    }

    /**
     * This loads the {@link Route}s.
     *
     * @param databaseBuilder Database from which to load the routes.
     */
    private void loadRoutes(Database.Builder databaseBuilder) {
        try {
            List<SQLiteAccess.ResultRow> routes = sqlite.executeStatement(
                    "SELECT id, connection_id FROM Route ORDER BY id, sequence_number"
            ).getRows();

            String lastId = null;
            Database.RouteBuilder routeBuilder = null;

            // rework into objects
            for (SQLiteAccess.ResultRow routeEntry : routes) {

                // read files from entry, mind index order (see columns above)
                String id = routeEntry.getString("id");
                String connectionId = routeEntry.getString("connection_id");

                // we need to group into our route object
                if (!id.equals(lastId)) {
                    if (routeBuilder != null) {
                        routeBuilder.create();
                    }
                    routeBuilder = databaseBuilder.addRoute(id);
                    lastId = id;
                }
                routeBuilder.addConnection(connectionId);
            }

            if (routeBuilder != null) {
                routeBuilder.create();
            }
        } catch (SQLException e) {
            log.warn("Error loading routes: {}. Skipping", e.getMessage());
        }
    }

}
