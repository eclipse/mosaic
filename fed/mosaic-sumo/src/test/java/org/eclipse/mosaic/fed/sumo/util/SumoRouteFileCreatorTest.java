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
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.junit.TestFileRule;
import org.eclipse.mosaic.rti.TIME;

import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;


/**
 * Tests the route writing.
 */
public class SumoRouteFileCreatorTest {

    private final static String ROUTES_FILE_XSD = "/xsd/routes_file.xsd";

    @Rule
    public TestFileRule testFileRule = new TestFileRule()
            .with("output.rou.xml", "/rou-file-with-veh-type.rou.xml");

    /**
     * Test whether a route file can be written error free.
     */
    @Test
    public void testRouteFileWriter() throws Exception {
        // SETUP: Prepare input data
        HashMap<String, VehicleType> types = new HashMap<>();
        types.put("myCar", new VehicleType("myCar"));
        types.put("hisCar", new VehicleType("hisCar"));

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

        // SETUP: Prepare Sumo routes
        Map<String, VehicleRoute> edgePaths = new HashMap<>();
        List<String> route = Lists.newArrayList("1", "5", "54", "6");
        edgePaths.put("23", new VehicleRoute("23", route, new ArrayList<>(), 0));
        edgePaths.put("24", new VehicleRoute("24", route, new ArrayList<>(), 0));

        // RUN
        SumoRouteFileCreator sumoRouteFileCreator = new SumoRouteFileCreator(testFileRule.get("output.rou.xml"), 0);
        sumoRouteFileCreator.addVehicleTypes(types).addRoutes(edgePaths);

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

        sumoRouteFileCreator.store(testFileRule.get("output.rou.xml"));

        // ASSERT
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(testFileRule.get("output.rou.xml"));
        assertNotNull(document);

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
        assertEquals("1.00", myCarVTypeAttributes.getNamedItem("speedFactor").getNodeValue());
        assertEquals("1.00", myCarVTypeAttributes.getNamedItem("tau").getNodeValue());
        assertEquals("passenger", myCarVTypeAttributes.getNamedItem("vClass").getNodeValue());
        Node hisCarVType = myCarVType.getNextSibling().getNextSibling();
        assertEquals("vType", hisCarVType.getNodeName());
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
        Node firstRoute = hisCarVType.getNextSibling().getNextSibling();
        assertEquals("route", firstRoute.getNodeName());
        NamedNodeMap firstRouteAttributes = firstRoute.getAttributes();
        assertEquals("1 5 54 6", firstRouteAttributes.getNamedItem("edges").getNodeValue());
        assertEquals("23", firstRouteAttributes.getNamedItem("id").getNodeValue());
        Node secondRoute = firstRoute.getNextSibling().getNextSibling();
        assertEquals("route", secondRoute.getNodeName());
        NamedNodeMap secondRouteAttributes = secondRoute.getAttributes();
        assertEquals("1 5 54 6", secondRouteAttributes.getNamedItem("edges").getNodeValue());
        assertEquals("24", secondRouteAttributes.getNamedItem("id").getNodeValue());
        Node firstVehicle = secondRoute.getNextSibling().getNextSibling();
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

        // validate against xsd
        validateXml();
    }

    private void validateXml() throws IOException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(this.getClass().getResource(ROUTES_FILE_XSD));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(testFileRule.get("output.rou.xml")));
    }
}