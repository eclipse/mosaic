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

import org.eclipse.mosaic.fed.sumo.config.CSumo;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.junit.TestFileRule;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
public class SumoVehicleTypesWriterTest {

    private final static String ROUTES_FILE_XSD = "/xsd/routes_file.xsd";

    @Rule
    public final TestFileRule testFileRule = new TestFileRule()
            .with("test.sumocfg", "/route-file-creator/test.sumocfg");

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
        Map<String, Map<String, String>> additionalVehicleTypeParameters = new HashMap<>();
        additionalVehicleTypeParameters.put("myCar", myCarAdditionalParameters);
        additionalVehicleTypeParameters.put("herCar", herCarAdditionalParameters);

        CSumo sumoConfiguration = new CSumo();
        sumoConfiguration.additionalVehicleTypeParameters = additionalVehicleTypeParameters;
        sumoConfiguration.timeGapOffset = 0;
        sumoConfiguration.sumoConfigurationFile = "test.sumocfg";

        // RUN
        SumoVehicleTypesWriter sumoVehicleTypesWriter = new SumoVehicleTypesWriter(testFileRule.getRoot(), sumoConfiguration);

        sumoVehicleTypesWriter.addVehicleTypes(types);

        sumoVehicleTypesWriter.store();
        // ASSERT
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new File(testFileRule.getRoot(), SumoVehicleTypesWriter.MOSAIC_TYPES_FILE_NAME));
        assertNotNull(document);
        NodeList vehicleTypesInDocument = document.getElementsByTagName("vType");
        assertEquals(vehicleTypesInDocument.getLength(), 2); // length should be 2 even though there are 3 types from sumo config
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

        // validate against xsd
        validateXml(new File(testFileRule.getRoot(), SumoVehicleTypesWriter.MOSAIC_TYPES_FILE_NAME));
    }

    private void validateXml(File routeFile) throws IOException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(this.getClass().getResource(ROUTES_FILE_XSD));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(routeFile));
    }
}