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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.junit.TestFileRule;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;


/**
 * Tests the route writing.
 */
public class SumoRouteFileCreatorTest {

    private final static String ROUTES_FILE_XSD = "/xsd/routes_file.xsd";

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final TestFileRule vehicleTypesRouteFile = new TestFileRule(temporaryFolder)
            .with("test_vTypes.rou.xml", "/route-file-creator/test_vTypes.rou.xml");

    private final TestFileRule departureFile = new TestFileRule(temporaryFolder)
            .with("departures.rou.xml", "/route-file-creator/departures.rou.xml");

    private final TestFileRule testSumoConfig = new TestFileRule(temporaryFolder)
            .with("test.sumocfg", "/route-file-creator/test.sumocfg");

    @Rule // chain both junit rules in a specific order
    public RuleChain testRules = RuleChain.outerRule(temporaryFolder)
            .around(vehicleTypesRouteFile)
            .around(departureFile)
            .around(testSumoConfig);

    /**
     * Test whether a route file can be written error free.
     */
    @Test
    public void testVehicleTypesWriting() throws Exception {
        // SETUP: Prepare input data
        HashMap<String, VehicleType> types = new HashMap<>();
        types.put("myCar", new VehicleType("myCar"));
        types.put("hisCar", new VehicleType("hisCar"));

        Map<String, String> myCarAdditionalParameters = new HashMap<>();
        myCarAdditionalParameters.put("color", "red");
        myCarAdditionalParameters.put("speedFactor", "1.20"); // testing overwriting
        Map<String, String> herCarAdditionalParameters = new HashMap<>();
        herCarAdditionalParameters.put("color", "red");
        Map<String, Map<String, String>> additionalVTypeParameters = new HashMap<>();
        additionalVTypeParameters.put("myCar", myCarAdditionalParameters);
        additionalVTypeParameters.put("herCar", herCarAdditionalParameters);
        // RUN
        SumoRouteFileCreator sumoRouteFileCreator = new SumoRouteFileCreator(
                testSumoConfig.get("test.sumocfg"), vehicleTypesRouteFile.get("test_vTypes.rou.xml"), additionalVTypeParameters, 0
        );

        sumoRouteFileCreator.addVehicleTypes(types);

        sumoRouteFileCreator.store(vehicleTypesRouteFile.get("test_vTypes.rou.xml"));
        // ASSERT
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(vehicleTypesRouteFile.get("test_vTypes.rou.xml"));
        assertNotNull(document);
        NodeList vehicleTypesInDocument = document.getElementsByTagName("vType");
        assertEquals(vehicleTypesInDocument.getLength(), 3); // it is also possible to add vTypes from sumo config
        // crawling XML and asserting right values
        Node routes = document.getFirstChild();
        assertEquals("routes", routes.getNodeName());
        Node myCarVType = routes.getFirstChild().getNextSibling();
        assertEquals("vType", myCarVType.getNodeName());
        NamedNodeMap myCarVTypeAttributes = myCarVType.getAttributes();
        assertEquals("myCar", myCarVTypeAttributes.getNamedItem("id").getNodeValue());
        assertEquals("2.60", myCarVTypeAttributes.getNamedItem("accel").getNodeValue());
        assertEquals("Krauss", myCarVTypeAttributes.getNamedItem("carFollowModel").getNodeValue());
        assertEquals("red", myCarVTypeAttributes.getNamedItem("color").getNodeValue());
        assertEquals("4.50", myCarVTypeAttributes.getNamedItem("emergencyDecel").getNodeValue());
        assertEquals("5.00", myCarVTypeAttributes.getNamedItem("length").getNodeValue());
        assertEquals("70.00", myCarVTypeAttributes.getNamedItem("maxSpeed").getNodeValue());
        assertEquals("2.50", myCarVTypeAttributes.getNamedItem("minGap").getNodeValue());
        assertEquals("0.50", myCarVTypeAttributes.getNamedItem("sigma").getNodeValue());
        assertEquals("1.20", myCarVTypeAttributes.getNamedItem("speedFactor").getNodeValue()); // test overwriting
        assertEquals("1.00", myCarVTypeAttributes.getNamedItem("tau").getNodeValue());
        assertEquals("passenger", myCarVTypeAttributes.getNamedItem("vClass").getNodeValue());
        Node herCarVType = myCarVType.getNextSibling().getNextSibling();
        assertEquals("vType", herCarVType.getNodeName());
        NamedNodeMap herCarVTypeAttributes = herCarVType.getAttributes();
        assertEquals("red", herCarVTypeAttributes.getNamedItem("color").getNodeValue());
        Node hisCarVType = herCarVType.getNextSibling().getNextSibling();
        NamedNodeMap hisCarVTypeAttributes = hisCarVType.getAttributes();
        assertEquals("hisCar", hisCarVTypeAttributes.getNamedItem("id").getNodeValue());
        assertEquals("2.60", hisCarVTypeAttributes.getNamedItem("accel").getNodeValue());
        assertEquals("Krauss", hisCarVTypeAttributes.getNamedItem("carFollowModel").getNodeValue());
        assertEquals("4.50", hisCarVTypeAttributes.getNamedItem("emergencyDecel").getNodeValue());
        assertEquals("5.00", hisCarVTypeAttributes.getNamedItem("length").getNodeValue());
        assertEquals("70.00", hisCarVTypeAttributes.getNamedItem("maxSpeed").getNodeValue());
        assertEquals("2.50", hisCarVTypeAttributes.getNamedItem("minGap").getNodeValue());
        assertEquals("0.50", hisCarVTypeAttributes.getNamedItem("sigma").getNodeValue());
        assertEquals("1.00", hisCarVTypeAttributes.getNamedItem("speedFactor").getNodeValue());
        assertEquals("1.00", hisCarVTypeAttributes.getNamedItem("tau").getNodeValue());
        assertEquals("passenger", hisCarVTypeAttributes.getNamedItem("vClass").getNodeValue());

        // validate against xsd
        validateXml(vehicleTypesRouteFile.get("test_vTypes.rou.xml"));
    }

    @Test
    public void testDepartureWriting() throws IOException, SAXException, ParserConfigurationException {
        SumoRouteFileCreator sumoRouteFileCreator = new SumoRouteFileCreator(
                testSumoConfig.get("test.sumocfg"), vehicleTypesRouteFile.get("test_vTypes.rou.xml"), null, 0
        );

        sumoRouteFileCreator.initializeDepartureDocument();

        // SETUP: scheduled departures
        ArrayList<VehicleDeparture> departures = new ArrayList<>();
        departures.add(new VehicleDeparture.Builder("23")
                .departureLane(VehicleDeparture.LaneSelectionMode.DEFAULT, 0, 0.0)
                .departureSpeed(30d)
                .create()
        );
        departures.add(new VehicleDeparture.Builder("23")
                .departureLane(VehicleDeparture.LaneSelectionMode.DEFAULT, 0, 0.0)
                .departureSpeed(30d)
                .create()

        ); // 50 seconds
        departures.add(new VehicleDeparture.Builder("24")
                .departureLane(VehicleDeparture.LaneSelectionMode.DEFAULT, 0, 0.0)
                .departureSpeed(30d)
                .create()
        ); // 100 seconds

        long time = 0L;
        int id = 0;
        for (VehicleDeparture vehicleDeparture : departures) {
            sumoRouteFileCreator.addVehicle(time * TIME.SECOND, "veh_" + id, "myCar",
                    vehicleDeparture.getRouteId(), Integer.toString(vehicleDeparture.getDepartureLane()),
                    String.format(Locale.ENGLISH, "%.2f", vehicleDeparture.getDeparturePos()),
                    String.format(Locale.ENGLISH, "%.2f", vehicleDeparture.getDepartSpeed())
            );
            time += 50L;
            id++;
        }

        sumoRouteFileCreator.storeDepartures(departureFile.get("departures.rou.xml"));

        // ASSERT
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(departureFile.get("departures.rou.xml"));
        assertNotNull(document);
        Node routes = document.getFirstChild();
        assertEquals("routes", routes.getNodeName());
        Node firstVehicle = routes.getFirstChild().getNextSibling();
        assertEquals("vehicle", firstVehicle.getNodeName());
        NamedNodeMap firstVehicleAttributes = firstVehicle.getAttributes();
        assertEquals("0.00", firstVehicleAttributes.getNamedItem("depart").getNodeValue());
        assertEquals("0", firstVehicleAttributes.getNamedItem("departLane").getNodeValue());
        assertEquals("0.00", firstVehicleAttributes.getNamedItem("departPos").getNodeValue());
        assertEquals("30.00", firstVehicleAttributes.getNamedItem("departSpeed").getNodeValue());
        assertEquals("veh_0", firstVehicleAttributes.getNamedItem("id").getNodeValue());
        assertEquals("23", firstVehicleAttributes.getNamedItem("route").getNodeValue());
        assertEquals("myCar", firstVehicleAttributes.getNamedItem("type").getNodeValue());
        Node secondVehicle = firstVehicle.getNextSibling().getNextSibling();
        assertEquals("vehicle", secondVehicle.getNodeName());
        NamedNodeMap secondVehicleAttributes = secondVehicle.getAttributes();
        assertEquals("50.00", secondVehicleAttributes.getNamedItem("depart").getNodeValue());
        assertEquals("0", secondVehicleAttributes.getNamedItem("departLane").getNodeValue());
        assertEquals("0.00", secondVehicleAttributes.getNamedItem("departPos").getNodeValue());
        assertEquals("30.00", secondVehicleAttributes.getNamedItem("departSpeed").getNodeValue());
        assertEquals("veh_1", secondVehicleAttributes.getNamedItem("id").getNodeValue());
        assertEquals("23", secondVehicleAttributes.getNamedItem("route").getNodeValue());
        assertEquals("myCar", secondVehicleAttributes.getNamedItem("type").getNodeValue());
        Node thirdVehicle = secondVehicle.getNextSibling().getNextSibling();
        assertEquals("vehicle", thirdVehicle.getNodeName());
        NamedNodeMap thirdVehicleAttributes = thirdVehicle.getAttributes();
        assertEquals("100.00", thirdVehicleAttributes.getNamedItem("depart").getNodeValue());
        assertEquals("0", thirdVehicleAttributes.getNamedItem("departLane").getNodeValue());
        assertEquals("0.00", thirdVehicleAttributes.getNamedItem("departPos").getNodeValue());
        assertEquals("30.00", thirdVehicleAttributes.getNamedItem("departSpeed").getNodeValue());
        assertEquals("veh_2", thirdVehicleAttributes.getNamedItem("id").getNodeValue());
        assertEquals("24", thirdVehicleAttributes.getNamedItem("route").getNodeValue());
        assertEquals("myCar", thirdVehicleAttributes.getNamedItem("type").getNodeValue());

        validateXml(departureFile.get("departures.rou.xml"));
    }

    private void validateXml(File routeFile) throws IOException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(this.getClass().getResource(ROUTES_FILE_XSD));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(routeFile));
    }
}