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

package org.eclipse.mosaic.fed.mapping.ambassador;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.mosaic.fed.mapping.config.CMappingAmbassador;
import org.eclipse.mosaic.fed.mapping.config.units.CTrafficLight;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.advanced.ScenarioTrafficLightRegistration;
import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLight;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightProgram;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.RtiAmbassador;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link SpawningFramework}.
 */
public class SpawningFrameworkTrafficLightTest {

    @Mock
    public RtiAmbassador rti;

    private RandomNumberGenerator rng;

    @Before
    public void setup() {
        rng = new DefaultRandomNumberGenerator(0L);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void initAppsWithoutWeight() throws InternalFederateException, IllegalValueException {
        //SETUP
        CMappingAmbassador mappingConfig = new CMappingAmbassador();
        mappingConfig.trafficLights = new ArrayList();
        mappingConfig.trafficLights.add(newTlConfig(null, null, "app_1"));
        mappingConfig.trafficLights.add(newTlConfig( null, null, "app_2"));
        ScenarioTrafficLightRegistration tlRegistration = newTlRegistration(1000);
        ArgumentCaptor<TrafficLightRegistration> captor = ArgumentCaptor.forClass(TrafficLightRegistration.class);

        //RUN
        SpawningFramework spawningFramework = new SpawningFramework(mappingConfig, tlRegistration, rti, rng);
        spawningFramework.timeAdvance(0, rti, rng);

        //ASSERT
        verify(rti, times(1000)).triggerInteraction(captor.capture());
        List <TrafficLightRegistration> tlrs = captor.getAllValues();
        Assert.assertEquals(countAppInTlr(tlrs, "app_1") / 1000d, 0.5d, 0.02d);
        Assert.assertEquals(countAppInTlr(tlrs, "app_2") / 1000d, 0.5d, 0.02d);
    }

    @Test
    public void initAppsWithWeightAndTlGroupId() throws InternalFederateException, IllegalValueException {
        //SETUP
        CMappingAmbassador mappingConfig = new CMappingAmbassador();
        mappingConfig.trafficLights = new ArrayList();
        mappingConfig.trafficLights.add(newTlConfig(null, 0.2, "app_1"));
        mappingConfig.trafficLights.add(newTlConfig( null, 0.8, "app_2"));
        mappingConfig.trafficLights.add(newTlConfig( "13", null, "app_3"));
        ScenarioTrafficLightRegistration tlRegistration = newTlRegistration(1000);
        ArgumentCaptor<TrafficLightRegistration> captor = ArgumentCaptor.forClass(TrafficLightRegistration.class);

        //RUN
        SpawningFramework spawningFramework = new SpawningFramework(mappingConfig, tlRegistration, rti, rng);
        spawningFramework.timeAdvance(0, rti, rng);

        //ASSERT
        verify(rti, times(1000)).triggerInteraction(captor.capture());
        List <TrafficLightRegistration> tlrs = captor.getAllValues();
        Assert.assertEquals(countAppInTlr(tlrs, "app_1") / 1000d, 0.2d, 0.02d);
        Assert.assertEquals(countAppInTlr(tlrs, "app_2") / 1000d, 0.8d, 0.02d);
        Assert.assertEquals(tlrs.get(13).getMapping().getApplications().get(0), "app_3");
    }

    private int countAppInTlr(List <TrafficLightRegistration> tlrs, String app){
        int app_occurences = 0;
        for (TrafficLightRegistration tlr : tlrs) {
            if (tlr.getMapping().getApplications().get(0).equals(app)) {
                app_occurences += 1;
            }
        }
        return app_occurences;
    }

    private CTrafficLight newTlConfig(String tlGroupId, Double weight, String application){
        CTrafficLight trafficLightConfiguration = new CTrafficLight();
        trafficLightConfiguration.tlGroupId = tlGroupId;
        trafficLightConfiguration.weight = weight;
        trafficLightConfiguration.applications = Arrays.asList(application);
        return trafficLightConfiguration;
    }

    public static ScenarioTrafficLightRegistration newTlRegistration(int numberTlGroups) {
        Map<String, Collection<String>> lanes = Maps.newHashMap();
        lanes.put("0", Lists.newArrayList("0"));
        Map<String, TrafficLightProgram> programs = new HashMap<>();
        List<TrafficLight> trafficLights = Lists.newArrayList(new TrafficLight(0, null, null, null, null));
        List<TrafficLightGroup> tlGroups = new ArrayList<>();
        for(int i=0; i<numberTlGroups; i++){
            tlGroups.add(new TrafficLightGroup(Integer.toString(i), programs , trafficLights));
        }
        return new ScenarioTrafficLightRegistration(0, tlGroups, lanes);
    }

}