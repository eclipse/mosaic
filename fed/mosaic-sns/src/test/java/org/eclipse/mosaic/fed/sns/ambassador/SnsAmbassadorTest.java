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

package org.eclipse.mosaic.fed.sns.ambassador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.interactions.communication.AdHocCommunicationConfiguration;
import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.objects.addressing.AdHocMessageRoutingBuilder;
import org.eclipse.mosaic.lib.objects.addressing.IpResolver;
import org.eclipse.mosaic.lib.objects.communication.AdHocConfiguration;
import org.eclipse.mosaic.lib.objects.communication.InterfaceConfiguration;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.RtiAmbassador;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Tests for {@link SnsAmbassador}.
 */
public class SnsAmbassadorTest {

    @Rule
    public IpResolverRule ipResolverRule = new IpResolverRule();

    @Rule
    public GeoProjectionRule projectionRule = new GeoProjectionRule(GeoPoint.latLon(52, 13));

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    public RtiAmbassador rtiMock;

    private SnsAmbassador ambassador;

    private final Map<String, GeoPoint> vehToPosition = new HashMap<>();
    private final List<V2xMessageReception> messagesSent = new ArrayList<>();

    @Before
    public void setup() throws Exception {
        final File configurationFile =
                new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("sns_config_complex_multihop.json")).toURI());

        final AmbassadorParameter ambassadorParameter = new AmbassadorParameter("sns", configurationFile);

        when(rtiMock.createRandomNumberGenerator()).thenReturn(new DefaultRandomNumberGenerator(89989123L));

        // Reset TransmissionSimulator
        ambassador = new SnsAmbassador(ambassadorParameter);
        ambassador.setRtiAmbassador(rtiMock);
        ambassador.initialize(0, 1000 * TIME.SECOND);

        doAnswer((invocationOnMock) -> {
            messagesSent.add(((V2xMessageReception) invocationOnMock.getArguments()[0]));
            return null;
        }).when(rtiMock).triggerInteraction(isA(V2xMessageReception.class));

    }

    @After
    public void tearDown() {
        SimulationEntities.INSTANCE.reset(); // reset Entities after every test
    }

    @Test
    public void topoBroadcast_differentSendingRadiusPerVehicle() throws InternalFederateException {
        //PREPARE
        addVehicle("veh_0");
        addVehicle("veh_1");
        addVehicle("veh_2");
        configureAdHoc("veh_0", 1337d);
        configureAdHoc("veh_1", 700d);
        configureAdHoc("veh_2", 250d); //use 250m from default configuration

        // Distance between vehicles: 680m
        moveVehicle("veh_0", GeoPoint.latLon(52.31, 13.41));
        moveVehicle("veh_1", GeoPoint.latLon(52.31, 13.40));
        moveVehicle("veh_2", GeoPoint.latLon(52.31, 13.39));

        //RUN + ASSERT
        AdHocMessageRoutingBuilder adHocMessageRoutingBuilder = new AdHocMessageRoutingBuilder("veh_0", vehToPosition.get("veh_0"));
        MessageRouting routing1 = adHocMessageRoutingBuilder.channel(AdHocChannel.CCH).broadcast().topological();
        sendMessage(routing1);
        assertReceivedMessages("veh_1");

        adHocMessageRoutingBuilder = new AdHocMessageRoutingBuilder("veh_1", vehToPosition.get("veh_1"));
        MessageRouting routing2 = adHocMessageRoutingBuilder.channel(AdHocChannel.CCH).broadcast().topological();
        sendMessage(routing2);
        assertReceivedMessages("veh_0", "veh_2");

        adHocMessageRoutingBuilder = new AdHocMessageRoutingBuilder("veh_2", vehToPosition.get("veh_2"));
        MessageRouting routing3 = adHocMessageRoutingBuilder.channel(AdHocChannel.CCH).broadcast().topological();

        sendMessage(routing3);
        assertReceivedMessages();
    }

    @Test
    public void geoBroadcast_vehiclesInRowWithCustomRadius() throws InternalFederateException {
        // PREPARE
        addVehicle("veh_0");
        addVehicle("veh_1");
        addVehicle("veh_2");
        configureAdHoc("veh_0", 70d);
        configureAdHoc("veh_1", 70d);
        configureAdHoc("veh_2", 70d);

        moveVehicle("veh_0", GeoPoint.latLon(52.31, 13.401));
        moveVehicle("veh_1", GeoPoint.latLon(52.31, 13.40));
        moveVehicle("veh_2", GeoPoint.latLon(52.31, 13.399));

        // RUN + ASSERT
        AdHocMessageRoutingBuilder adHocMessageRoutingBuilder = new AdHocMessageRoutingBuilder("veh_0", vehToPosition.get("veh_0"));
        MessageRouting routing1 = adHocMessageRoutingBuilder
                .channel(AdHocChannel.CCH)
                .broadcast().geographical(new GeoCircle(vehToPosition.get("veh_2"), 300));
        sendMessage(routing1);
        assertReceivedMessages("veh_1", "veh_2");

        // PREPARE reduce sending radius of middle vehicle resulting in insufficient forwarding power to reach veh_2
        configureAdHoc("veh_1", 10d); // use smaller range, than distance
        // RUN + ASSERT
        sendMessage(routing1);
        assertReceivedMessages("veh_1");

    }

    private void assertReceivedMessages(String... vehicleNames) {
        Set<String> sent = new HashSet<>();
        for (V2xMessageReception message : messagesSent) {
            sent.add(message.getReceiverName());
        }

        assertEquals(vehicleNames.length, sent.size());
        for (String vehicleName : vehicleNames) {
            assertTrue(sent.contains(vehicleName));
        }
    }

    private void sendMessage(MessageRouting routing) throws InternalFederateException {
        messagesSent.clear();

        V2xMessage v2xMessage = new V2xMessage(routing) {
            @Nonnull
            @Override
            public EncodedPayload getPayload() {
                return new EncodedPayload(0);
            }
        };
        V2xMessageTransmission v2xMessageTransmission = new V2xMessageTransmission(0, v2xMessage);
        ambassador.processInteraction(v2xMessageTransmission);
        ambassador.advanceTime(200 * TIME.NANO_SECOND); // advance time so processTimeAdvanceGrant is called
    }

    private void configureAdHoc(String vehicleName, Double radius) throws InternalFederateException {
        AdHocCommunicationConfiguration adHocCommunication = new AdHocCommunicationConfiguration(0,
                new AdHocConfiguration.Builder(vehicleName)
                        .addInterface(new InterfaceConfiguration.Builder(AdHocChannel.CCH)
                                .ip(IpResolver.getSingleton().registerHost(vehicleName))
                                .subnet(IpResolver.getSingleton().getNetMask())
                                .radius(radius)
                                .create()
                        ).create()
        );
        ambassador.processInteraction(adHocCommunication);
    }

    private void addVehicle(String vehicleName) throws InternalFederateException {
        VehicleRegistration vehicleRegistration = new VehicleRegistration(0, vehicleName, null, Lists.newArrayList("app"),
                new VehicleDeparture.Builder("0").create(), new VehicleType("default"));
        ambassador.processInteraction(vehicleRegistration);

        IpResolver.getSingleton().registerHost(vehicleName);
    }

    private void moveVehicle(String vehicle, GeoPoint position) throws InternalFederateException {
        VehicleData vehicleData = new VehicleData.Builder(0, vehicle).position(position, position.toCartesian()).create();
        VehicleUpdates vehicleUpdates = new VehicleUpdates(0, Lists.newArrayList(), Lists.newArrayList(vehicleData), Lists.newArrayList());
        ambassador.processInteraction(vehicleUpdates);

        vehToPosition.put(vehicle, position);
    }

}
