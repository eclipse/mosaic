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
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.ColorUtils;
import org.eclipse.mosaic.rti.TIME;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
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

    /**
     * Document used to write vehicle types (prototypes) from Mapping to.
     * Note: parameters from {@link org.eclipse.mosaic.fed.sumo.config.CSumo#additionalVTypeParameters}
     * will also be written into this file, overwriting (TODO) values from Mapping.
     */
    private final Document vehicleTypesDocument;

    /**
     * Document used to write departures to.
     */
    private Document departureDocument;

    /**
     * Stores additional vehicle type parameters specified in {@link org.eclipse.mosaic.fed.sumo.config.CSumo}.
     */
    private final Map<String, Map<String, String>> additionalVTypeParameters;

    /**
     * Constructor for {@link SumoRouteFileCreator}.
     *
     * @param vehicleTypeRouteFile the name for the newly written route-file
     * @param timeGapOffset        used to add time Offset to vehicle types
     */
    public SumoRouteFileCreator(File sumoConfigurationFile,
                                File vehicleTypeRouteFile,
                                Map<String, Map<String, String>> additionalVTypeParameters,
                                double timeGapOffset) {
        if (vehicleTypeRouteFile == null) {
            throw new IllegalArgumentException("No route file given.");
        }

        this.additionalVTypeParameters = additionalVTypeParameters;
        this.timeGapOffset = timeGapOffset;

        addVehicleTypeRouteFileToSumoConfig(sumoConfigurationFile, vehicleTypeRouteFile);
        vehicleTypesDocument = initializeDocument();
    }

    /**
     * Adds the new route file containing the vehicle type to the sumo configuration.
     *
     * @param sumoConfigurationFile    the sumo configuration
     * @param baseVehicleTypeRouteFile the route file containing the vehicle types, used to get name
     */
    private void addVehicleTypeRouteFileToSumoConfig(File sumoConfigurationFile, File baseVehicleTypeRouteFile) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(sumoConfigurationFile);

            Node routeFilesNode = doc.getElementsByTagName("route-files").item(0);
            Element routeFilesNodeElement = (Element) routeFilesNode;
            String previousRouteFiles = routeFilesNodeElement.getAttribute("value");
            previousRouteFiles = previousRouteFiles.isEmpty() ? "" : "," + previousRouteFiles; // "" if there were no previous route files
            routeFilesNodeElement.setAttribute("value", baseVehicleTypeRouteFile.getName() + previousRouteFiles);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(sumoConfigurationFile);
            Source input = new DOMSource(doc);
            transformer.transform(input, output);
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the document and writes the route-tag into it.
     */
    private Document initializeDocument() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            // set meta tags
            document.setXmlVersion("1.0");
            // initialize parent node
            Node parent = document.createElement("routes");
            document.appendChild(parent);
            return document;
        } catch (ParserConfigurationException e) {
            log.warn("Couldn't instantiate DocumentBuilder, this will result in no Route-File being written");
        }
        return null;
    }

    /**
     * Adds the given vehicle types including the already existing
     * to the {@link #departureDocument}.
     */
    @SuppressWarnings("UnusedReturnValue")
    public SumoRouteFileCreator addVehicleTypes(Map<String, VehicleType> additionalVTypes) {
        // adding new types with additional parameters from sumo config
        Map<String, Map<String, String>> newVTypes = generateAttributesMap(additionalVTypes, timeGapOffset);

        // applies the additional para
        applyParametersFromSumoConfiguration(newVTypes);

        // write vehicle type to new route file containing vehicle types
        writeVehicleTypes(newVTypes);

        return this;
    }

    private void applyParametersFromSumoConfiguration(Map<String, Map<String, String>> newVTypes) {
        for (Entry<String, Map<String, String>> sumoParameterEntry : additionalVTypeParameters.entrySet()) {
            String currentVehicleType = sumoParameterEntry.getKey();
            newVTypes.putIfAbsent(currentVehicleType, new HashMap<>());
            Set<Entry<String, String>> additionalParameters = sumoParameterEntry.getValue().entrySet();
            for (Entry<String, String> parameter : additionalParameters) {
                String parameterName = parameter.getKey();
                String parameterValue = parameter.getValue();
                newVTypes.get(currentVehicleType).put(parameterName, parameterValue);
            }
        }
    }

    private void writeVehicleTypes(Map<String, Map<String, String>> newVTypes) {
        Node routesNode = vehicleTypesDocument.getFirstChild();
        if (!routesNode.getNodeName().equals("routes")) {
            log.warn("Couldn't write vTypes");
            return;
        }
        for (Entry<String, Map<String, String>> attributesEntry : newVTypes.entrySet()) {
            Element currentVType = vehicleTypesDocument.createElement("vType");
            currentVType.setAttribute("id", attributesEntry.getKey());
            for (Entry<String, String> attributeEntry : attributesEntry.getValue().entrySet()) {
                String attributeName = attributeEntry.getKey();
                String attributeValue = attributeEntry.getValue();

                currentVType.setAttribute(attributeName, attributeValue);

            }
            routesNode.appendChild(currentVType);
        }
    }

    /**
     * Generates a Map with the attributes of the vehicle.
     *
     * @param actualTypes Represents the vehicle types.
     * @return Attributes of a vehicle type.
     */
    private Map<String, Map<String, String>> generateAttributesMap(Map<String, VehicleType> actualTypes, double timeGapOffset) {
        Map<String, Map<String, String>> types = new HashMap<>();

        Map<String, String> attributes;
        for (Entry<String, VehicleType> vehicleTypesEntry : actualTypes.entrySet()) {
            attributes = types.computeIfAbsent(vehicleTypesEntry.getKey(), k -> new TreeMap<>());

            attributes.put("vClass", SumoVehicleClassMapping.toSumo(vehicleTypesEntry.getValue().getVehicleClass()));
            attributes.put("accel", formatDoubleAttribute(vehicleTypesEntry.getValue().getAccel()));
            attributes.put("decel", formatDoubleAttribute(vehicleTypesEntry.getValue().getDecel()));
            attributes.put("emergencyDecel", formatDoubleAttribute(vehicleTypesEntry.getValue().getEmergencyDecel()));
            attributes.put("length", formatDoubleAttribute(vehicleTypesEntry.getValue().getLength()));
            attributes.put("maxSpeed", formatDoubleAttribute(vehicleTypesEntry.getValue().getMaxSpeed()));
            attributes.put("minGap", formatDoubleAttribute(vehicleTypesEntry.getValue().getMinGap()));
            attributes.put("sigma", formatDoubleAttribute(vehicleTypesEntry.getValue().getSigma()));
            attributes.put("tau", formatDoubleAttribute(vehicleTypesEntry.getValue().getTau() + timeGapOffset));
            attributes.put("speedFactor", formatDoubleAttribute(vehicleTypesEntry.getValue().getSpeedFactor()));

            // We deviate the speedFactor on our own. Therefore, we must set the speedDev in SUMO to 0.0 in order
            // to not alter the already adjusted speedFactor again.
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
        return types;
    }

    private String formatDoubleAttribute(double attribute) {
        return String.format(Locale.ENGLISH, "%.2f", attribute);
    }

    public boolean departuresInitialized() {
        return departureDocument != null;
    }

    public void initializeDepartureDocument() {
        departureDocument = initializeDocument();
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
            Node routesNode = departureDocument.getFirstChild();
            if (!routesNode.getNodeName().equals("routes")) {
                throw new Exception();
            }

            // validating parameters
            // create element and add attributes
            Element vehicleElement = departureDocument.createElement("vehicle");

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
        writeXmlFile(target, vehicleTypesDocument);
    }

    /**
     * Stores the document to the given target file.
     */
    public void storeDepartures(File target) {
        // write route-file
        writeXmlFile(target, departureDocument);
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
            log.info("{} file successfully written.", file.toString());
        } catch (TransformerException e) {
            log.debug("Couldn't write route-file.");
        }
    }
}
