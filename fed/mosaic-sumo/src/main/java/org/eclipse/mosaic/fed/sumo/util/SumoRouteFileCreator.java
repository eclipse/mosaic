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

import org.eclipse.mosaic.fed.sumo.config.CSumo;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.ColorUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
 * This class creates a new SUMO route file containing vehicle types added from Mapping and link to that file in the sumo configuration.
 * Additionally it will merge vehicle types from mapping with additional parameters specified
 * in {@link CSumo#additionalVehicleTypeParameters}.
 */
public class SumoRouteFileCreator {

    private final static Logger log = LoggerFactory.getLogger(SumoRouteFileCreator.class);

    /**
     * Document used to write vehicle types (prototypes) from Mapping to.
     * Note: parameters from {@link org.eclipse.mosaic.fed.sumo.config.CSumo#additionalVehicleTypeParameters}
     * will also be written into this file, overwriting values from Mapping.
     */
    private final Document vehicleTypesDocument;

    private final double timeGapOffset;

    /**
     * Stores additional vehicle type parameters specified in {@link org.eclipse.mosaic.fed.sumo.config.CSumo}.
     */
    private final Map<String, Map<String, String>> additionalVehicleTypeParameters;

    /**
     * Constructor for {@link SumoRouteFileCreator}.
     *
     * @param sumoConfigurationFile this is the {@link File}-object linking to the `.sumocfg`-file
     * @param vehicleTypeRouteFile  the name for the newly written route-file
     * @param sumoConfiguration     the sumo configuration read from sumo_config.json
     */
    public SumoRouteFileCreator(File sumoConfigurationFile,
                                File vehicleTypeRouteFile,
                                CSumo sumoConfiguration) {
        if (vehicleTypeRouteFile == null) {
            throw new IllegalArgumentException("No vehicle type route file given.");
        }

        this.additionalVehicleTypeParameters = sumoConfiguration.additionalVehicleTypeParameters;
        this.timeGapOffset = sumoConfiguration.timeGapOffset;

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

            NodeList routeFilesNodes = doc.getElementsByTagName("route-files");
            Element routeFilesNodeElement;
            String newRouteFilesValue;
            if (routeFilesNodes.getLength() == 0) { // if there is no "route-files" element we have to create it and add
                Node newRouteFilesNode = doc.createElement("route-files");

                Element inputNode = (Element) doc.getElementsByTagName("input").item(0);
                inputNode.appendChild(newRouteFilesNode);
                routeFilesNodeElement = (Element) routeFilesNodes.item(0);
                newRouteFilesValue = baseVehicleTypeRouteFile.getName();
            } else {
                routeFilesNodeElement = (Element) routeFilesNodes.item(0);
                String previousRouteFiles = routeFilesNodeElement.getAttribute("value") + ",";
                newRouteFilesValue = previousRouteFiles + baseVehicleTypeRouteFile.getName();
            }
            routeFilesNodeElement.setAttribute("value", newRouteFilesValue);


            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(sumoConfigurationFile);
            Source input = new DOMSource(doc);
            transformer.transform(input, output);
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            log.error("Couldn't add new vehicle Types to SUMO configuration.", e);
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
     * {@link #vehicleTypesDocument}.
     */
    @SuppressWarnings("UnusedReturnValue")
    public SumoRouteFileCreator addVehicleTypes(Map<String, VehicleType> additionalVehicleTypes) {
        // adding new types with additional parameters from sumo config
        Map<String, Map<String, String>> newVehicleTypes = generateAttributesMap(additionalVehicleTypes, timeGapOffset);

        // applies the additional para
        applyParametersFromSumoConfiguration(newVehicleTypes);

        // write vehicle type to new route file containing vehicle types
        writeVehicleTypes(newVehicleTypes);

        return this;
    }

    private void applyParametersFromSumoConfiguration(Map<String, Map<String, String>> newVehicleTypes) {
        for (Entry<String, Map<String, String>> sumoParameterEntry : additionalVehicleTypeParameters.entrySet()) {
            String currentVehicleType = sumoParameterEntry.getKey();
            if (!newVehicleTypes.containsKey(currentVehicleType)) {
                continue; // if type defined in sumo config wasn't defined in mapping ignore the type
            }
            Set<Entry<String, String>> additionalParameters = sumoParameterEntry.getValue().entrySet();
            for (Entry<String, String> parameter : additionalParameters) {
                String parameterName = parameter.getKey();
                String parameterValue = parameter.getValue();
                newVehicleTypes.get(currentVehicleType).put(parameterName, parameterValue);
            }
        }
    }

    private void writeVehicleTypes(Map<String, Map<String, String>> newVehicleTypes) {
        Node routesNode = vehicleTypesDocument.getFirstChild();
        if (!routesNode.getNodeName().equals("routes")) {
            log.warn("Couldn't write vTypes");
            return;
        }
        for (Entry<String, Map<String, String>> attributesEntry : newVehicleTypes.entrySet()) {
            Element currentVehicleType = vehicleTypesDocument.createElement("vType");
            currentVehicleType.setAttribute("id", attributesEntry.getKey());
            for (Entry<String, String> attributeEntry : attributesEntry.getValue().entrySet()) {
                String attributeName = attributeEntry.getKey();
                String attributeValue = attributeEntry.getValue();

                currentVehicleType.setAttribute(attributeName, attributeValue);

            }
            routesNode.appendChild(currentVehicleType);
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

    /**
     * Stores the document to the given target file.
     */
    public void store(File target) {
        // write route-file
        writeXmlFile(target, vehicleTypesDocument);
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
