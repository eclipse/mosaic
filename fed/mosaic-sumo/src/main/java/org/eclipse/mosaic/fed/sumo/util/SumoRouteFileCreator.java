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

package org.eclipse.mosaic.fed.sumo.util;

import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.ColorUtils;
import org.eclipse.mosaic.rti.TIME;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * This class is used to write Sumo vehicle definitions and routes into a so
 * called rou.xml file.
 */
public class SumoRouteFileCreator {

    private final static Logger log = LoggerFactory.getLogger(SumoRouteFileCreator.class);

    private final double timeGapOffset;

    private Document routeFileDocument;

    private Map<String, Map<String, String>> vehicleTypesWithAttributes;

    private Map<String, VehicleRoute> routes;


    /**
     * Constructor for {@link SumoRouteFileCreator}.
     *
     * @param baseRouteFile the route file to read from / write to
     * @param timeGapOffset used to add time Offset to vehicle types
     */
    public SumoRouteFileCreator(File baseRouteFile, double timeGapOffset) {
        if (baseRouteFile == null) {
            throw new IllegalArgumentException("No route file given.");
        }
        this.timeGapOffset = timeGapOffset;

        initializeDocument();
        extractVehicleTypesAndRoutes(baseRouteFile);

        // deleting prior route file
        if (baseRouteFile.exists() && !baseRouteFile.delete()) {
            log.warn("Could not delete file {}", baseRouteFile.getPath());
        }
    }

    /**
     * Initializes the document and writes the route-tag into it.
     */
    private void initializeDocument() {

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            routeFileDocument = documentBuilder.newDocument();
            // set meta tags
            routeFileDocument.setXmlVersion("1.0");
            // initialize parent node
            Node parent = routeFileDocument.createElement("routes");
            routeFileDocument.appendChild(parent);
        } catch (ParserConfigurationException e) {
            log.warn("Couldn't instantiate DocumentBuilder, this will result in no Route-File being written");
        }
    }

    /**
     * Stores the existing types and routes from the given file before deletion.
     */
    private void extractVehicleTypesAndRoutes(File baseRouteFile) {
        log.info("Reading routes and vTypes before Rewriting.");
        vehicleTypesWithAttributes = readVehicleTypes(baseRouteFile);
        routes = readRoutes(baseRouteFile);
    }

    private Map<String, Map<String, String>> readVehicleTypes(File baseRouteFile) {
        VehicleTypeParser vehTypeParser = new VehicleTypeParser();

        if (baseRouteFile != null && baseRouteFile.exists()) {
            try (InputStream input = new FileInputStream(baseRouteFile)) {
                SAXParserFactory.newInstance().newSAXParser().parse(input, vehTypeParser);
            } catch (Exception e) {
                log.error("Could not read vehicle type definition", e);
            }
        }
        return vehTypeParser.vehTypeAttributes;
    }

    /**
     * This method reads vehicle routes from the rou-file.
     *
     * @return routes with their IDs.
     */
    private Map<String, VehicleRoute> readRoutes(File baseRouteFile) {
        RouteParser routeParser = new RouteParser();

        if (baseRouteFile != null && baseRouteFile.exists()) {
            try (InputStream input = new FileInputStream(baseRouteFile)) {
                SAXParserFactory.newInstance().newSAXParser().parse(input, routeParser);
            } catch (Exception e) {
                log.error("Could not read vehicle type definition", e);
            }
        }
        return routeParser.vehicleRoutes;
    }

    /**
     * Adds the given vehicle types including the already existing
     * to the {@link #routeFileDocument}.
     */
    @SuppressWarnings("UnusedReturnValue")
    public SumoRouteFileCreator addVehicleTypes(Map<String, VehicleType> additionalVTypes) {
        // adding / overwriting given types
        Map<String, Map<String, String>> newVTypes =
                generateAttributesMap(vehicleTypesWithAttributes, additionalVTypes, timeGapOffset);
        mergeVTypes(newVTypes);

        // Write vehicle types
        Node routesNode = routeFileDocument.getFirstChild();
        if (!routesNode.getNodeName().equals("routes")) {
            log.warn("Couldn't write vTypes");
            return this;
        }
        for (Entry<String, Map<String, String>> attributesEntry : vehicleTypesWithAttributes.entrySet()) {
            Element currentVType = routeFileDocument.createElement("vType");
            currentVType.setAttribute("id", attributesEntry.getKey());
            for (Entry<String, String> attributeEntry : attributesEntry.getValue().entrySet()) {
                currentVType.setAttribute(attributeEntry.getKey(), attributeEntry.getValue());
            }
            routesNode.appendChild(currentVType);
        }
        return this;
    }

    /**
     * Checks if newly added vTypes already existed and overwrites all new attributes,
     * while keeping the old ones.
     *
     * @param newVTypes map of the new vTypes
     */
    private void mergeVTypes(Map<String, Map<String, String>> newVTypes) {
        for (Entry<String, Map<String, String>> typeDef : newVTypes.entrySet()) {
            if (vehicleTypesWithAttributes.containsKey(typeDef.getKey())) {
                // use old Vtype definitions to overwrite
                Map<String, String> oldVtype = vehicleTypesWithAttributes.get(typeDef.getKey());
                oldVtype.putAll(typeDef.getValue());
                // set updated Vtype
                vehicleTypesWithAttributes.put(typeDef.getKey(), oldVtype);
            } else {
                vehicleTypesWithAttributes.put(typeDef.getKey(), typeDef.getValue());
            }
        }
    }

    /**
     * Generates a Map with the attributes of the vehicle.
     *
     * @param actualTypes Represents the vehicle types.
     * @return Attributes of a vehicle type.
     */
    private Map<String, Map<String, String>> generateAttributesMap(Map<String, Map<String, String>> predefinedTypes,
                                                                  Map<String, VehicleType> actualTypes, double timeGapOffset) {
        Map<String, Map<String, String>> mergedTypes = new HashMap<>(predefinedTypes);

        Map<String, String> attributes;
        for (Entry<String, VehicleType> vehicleTypesEntry : actualTypes.entrySet()) {
            attributes = mergedTypes.computeIfAbsent(vehicleTypesEntry.getKey(), k -> new TreeMap<>());

            attributes.put("vClass", SumoVehicleClassMapping.toSumo(vehicleTypesEntry.getValue().getVehicleClass()));
            attributes.put("accel", String.format(Locale.ENGLISH, "%.2f", vehicleTypesEntry.getValue().getAccel()));
            attributes.put("decel", String.format(Locale.ENGLISH, "%.2f", vehicleTypesEntry.getValue().getDecel()));
            attributes.put("emergencyDecel", String.format(Locale.ENGLISH, "%.2f", vehicleTypesEntry.getValue().getEmergencyDecel()));
            attributes.put("length", String.format(Locale.ENGLISH, "%.2f", vehicleTypesEntry.getValue().getLength()));
            attributes.put("maxSpeed", String.format(Locale.ENGLISH, "%.2f", vehicleTypesEntry.getValue().getMaxSpeed()));
            attributes.put("minGap", String.format(Locale.ENGLISH, "%.2f", vehicleTypesEntry.getValue().getMinGap()));
            attributes.put("sigma", String.format(Locale.ENGLISH, "%.2f", vehicleTypesEntry.getValue().getSigma()));
            attributes.put(
                    "tau",
                    String.format(Locale.ENGLISH, "%.2f", vehicleTypesEntry.getValue().getTau() + timeGapOffset)
            );
            attributes.put("speedFactor", String.format(Locale.ENGLISH, "%.2f", vehicleTypesEntry.getValue().getSpeedFactor()));

            /*
             * We deviate the speedFactor on our own. Therefore, we must set the speedDev in SUMO to 0.0 in order
             * to not alter the already adjusted speedFactor again.
             */
            attributes.put("speedDev", "0.0");

            Color color = ColorUtils.toColor(vehicleTypesEntry.getValue().getColor(), null);
            if (color != null) {
                attributes.putIfAbsent(
                        "color",
                        String.format(Locale.ENGLISH, "%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue())
                );
            }
            attributes.putIfAbsent(
                    "carFollowModel",
                    vehicleTypesEntry.getValue().getVehicleClass() == VehicleClass.AutomatedVehicle ? "ACC" : "Krauss"
            );
            if (vehicleTypesEntry.getValue().getVehicleClass() == VehicleClass.ElectricVehicle) {
                attributes.putIfAbsent("emissionClass", "Energy/unknown");
            }
        }
        return mergedTypes;
    }

    /**
     * Adds the given routes to the {@link #routeFileDocument}.
     *
     * @param additionalRoutes routes to be added
     */
    @SuppressWarnings("UnusedReturnValue")
    public SumoRouteFileCreator addRoutes(final Map<String, VehicleRoute> additionalRoutes) {
        // adding / overwriting existing routes with given routes
        routes.putAll(additionalRoutes);
        // Write routes for vehicle initialization
        Node routesNode = routeFileDocument.getFirstChild();
        if (!routesNode.getNodeName().equals("routes")) {
            log.warn("Couldn't write Routes");
            return this;
        }
        for (VehicleRoute route : routes.values()) {
            Element currentRoute = routeFileDocument.createElement("route");
            currentRoute.setAttribute("id", route.getId());
            currentRoute.setAttribute("edges", Joiner.on(" ").join(route.getEdgeIdList()));
            routesNode.appendChild(currentRoute);
        }
        return this;
    }


    /**
     * Adds a vehicle with the given parameters to the route file.
     *
     * @param time        time of the departure
     * @param vehicleId   the id of the vehicle
     * @param vehicleType string representation of the vehicle type
     * @param routeId     the id of the route, that the vehicle follows
     * @param laneId      the lane, that the vehicle spawns on
     * @param departPos   the position, the vehicle departs from
     * @param departSpeed the speed the vehicle departs with
     */
    @SuppressWarnings("UnusedReturnValue")
    public SumoRouteFileCreator addVehicle(long time, String vehicleId, String vehicleType,
                                           String routeId, String laneId, String departPos, String departSpeed) {
        try {
            Node routesNode = routeFileDocument.getFirstChild();
            if (!routesNode.getNodeName().equals("routes")) {
                throw new Exception();
            }

            // validating parameters
            // create element and add attributes
            Element vehicleElement = routeFileDocument.createElement("vehicle");

            vehicleElement.setAttribute("id", vehicleId);
            vehicleElement.setAttribute("type", vehicleType);
            vehicleElement.setAttribute("route", routeId);
            vehicleElement.setAttribute("depart", String.format(Locale.ENGLISH, "%.2f", (time / (double) TIME.SECOND)));
            vehicleElement.setAttribute("departPos", departPos);
            vehicleElement.setAttribute("departSpeed", departSpeed);
            vehicleElement.setAttribute("departLane", laneId);

            routesNode.appendChild(vehicleElement);
        } catch (IOException e) {
            log.warn("Couldn't open given file.");
        } catch (Exception e) {
            log.warn("The given file isn't a proper rou.xml file.");
        }
        return this;
    }

    /**
     * Stores the document to the given target file.
     */
    public void store(File target) {
        // write route-file
        writeXmlFile(target, routeFileDocument);
    }

    /**
     * Merges the document into the existing file.
     *
     * @param file     file to write to
     * @param document {@link Document} object, containing xml-description
     */
    private void writeXmlFile(File file, Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
            log.info("{}-file successfully written.", file.toString());
        } catch (TransformerException e) {
            log.debug("Couldn't write route-file.");
        }
    }


    /**
     * SAX Handler which extracts all additional information about vehicle types from the given route-file.
     * Read all attribute/values from the route-file for each vehicle type in that file.
     * Overwrite values with those from injected {@link VehicleType}s (from mapping).
     * Additional information such as "color" will be kept.
     */
    static class VehicleTypeParser extends DefaultHandler {

        private final Map<String, Map<String, String>> vehTypeAttributes;

        VehicleTypeParser() {
            vehTypeAttributes = new HashMap<>();
        }

        @Override
        public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
            if ("vType".equals(qualifiedName)) {
                Map<String, String> attributesMap = new TreeMap<>();
                String attributeKey;
                String attributeValue;
                for (int i = 0; i < attributes.getLength(); i++) {
                    attributeKey = attributes.getQName(i);
                    attributeValue = attributes.getValue(i);
                    if (!"id".equals(attributeKey)) {
                        attributesMap.put(attributeKey, attributeValue);
                    } else {
                        vehTypeAttributes.put(attributeValue, attributesMap);
                    }
                }
            }
        }
    }

    /**
     * SAX Handler which extracts all route information from the given route-file.
     */
    static class RouteParser extends DefaultHandler {

        private final Map<String, VehicleRoute> vehicleRoutes;

        RouteParser() {
            vehicleRoutes = new HashMap<>();
        }

        @Override
        public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) {
            if ("route".equals(qualifiedName)) {
                final String id = attributes.getValue("id");
                final String edges = attributes.getValue("edges");
                vehicleRoutes.put(id, new VehicleRoute(id, Lists.newArrayList(StringUtils.split(edges, " ")), null, 0));
            }
        }
    }
}
