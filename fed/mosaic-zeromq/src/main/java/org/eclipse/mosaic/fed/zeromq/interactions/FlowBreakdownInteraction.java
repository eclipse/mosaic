
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

package org.eclipse.mosaic.fed.zeromq.interactions;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.UnitData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.eclipse.mosaic.interactions.application.ApplicationInteraction;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.rti.api.Interaction;

import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.text.DefaultEditorKit.CutAction;

import com.google.common.collect.Multiset.Entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Random;

public final class FlowBreakdownInteraction extends ApplicationInteraction {

    private String breakdownRoadId;
    private double resultedSpeed;
    private Random rand = new Random();

    public final static String TYPE_ID = createTypeIdentifier(FlowBreakdownInteraction.class);

    public FlowBreakdownInteraction(long time, String unitId, VehicleUpdates interaction) {
        super(time, unitId);
        determineRoadId(interaction);
    }

    public String getBreakdownRoadId() {
        return breakdownRoadId;
    }

    public double getResultedSpeed() {
        return resultedSpeed;
    }

    @Override
    public String getUnitId() {
        return super.getUnitId();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private void determineRoadId(VehicleUpdates interaction){

        TreeMap<Integer, String> countRoadDensity = new TreeMap<Integer, String>();
        ArrayList<String> roadIdList = new ArrayList<String>();

        for (VehicleData veh : interaction.getUpdated()){
            roadIdList.add(veh.getRoadPosition().getConnectionId());
        }

        for (String road : roadIdList) {
            Integer occurrences = Collections.frequency(roadIdList, road);
            try {
                countRoadDensity.put(occurrences, road);
            }catch (ClassCastException e) {
            }
        }

        Integer headKey = countRoadDensity.firstKey();
        Double headTopPart = headKey * 0.2;
        SortedMap<Integer, String> sorted = countRoadDensity.tailMap(headTopPart.intValue());
        List<String> candidateRoads = new ArrayList<String>(sorted.values());

        String selectedRoad = candidateRoads.get(rand.nextInt(candidateRoads.size()));
        this.breakdownRoadId = selectedRoad;    
        this.resultedSpeed = rand.nextInt(3) + 1;
    }

}
