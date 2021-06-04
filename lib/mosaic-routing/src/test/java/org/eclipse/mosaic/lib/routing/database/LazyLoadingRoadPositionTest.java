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

package org.eclipse.mosaic.lib.routing.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.objects.road.IConnection;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class LazyLoadingRoadPositionTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final static String dbFile = "/tiergarten.db";

    private Database database;

    @Mock
    INode previous;
    @Mock
    INode upcoming;
    @Mock
    IRoadPosition roadPosition;

    @Before
    public void setup() throws IOException {
        final File dbFileCopy = folder.newFile("tiergarten.db");

        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream(dbFile), dbFileCopy);

        database = Database.loadFromFile(dbFileCopy);

        when(roadPosition.getOffset()).thenReturn(13.37d);
    }

    @Test
    public void sumoAvailableValues_everythingAvailable() {
        //SETUP
        when(roadPosition.getConnectionId()).thenReturn("4068038_251150126_428788319");

        //RUN
        final LazyLoadingRoadPosition refinedRoadPosition = new LazyLoadingRoadPosition(roadPosition, database);

        //ASSERT
        assertRefinedWay(refinedRoadPosition);
        assertRefinedStartJunction(refinedRoadPosition);
        assertRefinedEndJunction(refinedRoadPosition);
        assertRefinedRoadSegment(refinedRoadPosition);
        assertRefinedPrevious(refinedRoadPosition);
        assertRefinedUpcoming(refinedRoadPosition);

        assertEquals("4068038_251150126_428788319", refinedRoadPosition.getConnection().getId());
        Collection<IConnection> outgoingConnections = refinedRoadPosition.getConnection().getOutgoingConnections();
        assertFalse(outgoingConnections.isEmpty());
        List<String> outgoingConnectionsIds = outgoingConnections.stream().map(IConnection::getId).collect(Collectors.toList());
        assertEquals("36878113_428788319_428788320", outgoingConnectionsIds.get(0));
        assertEquals("4068038_428788319_408194194", outgoingConnectionsIds.get(1));

        Collection<IConnection> incomingConnections = refinedRoadPosition.getConnection().getIncomingConnections();
        assertFalse(incomingConnections.isEmpty());
        List<String> incomingConnectionsIds = incomingConnections.stream().map(IConnection::getId).collect(Collectors.toList());
        assertEquals("4068038_21487168_251150126", incomingConnectionsIds.get(0));
        assertEquals("36338285_415838100_251150126", incomingConnectionsIds.get(1));
    }

    @Test
    public void phabmacsAvailableValues_everythingAvailable() {
        //SETUP
        when(roadPosition.getPreviousNode()).thenReturn(previous);
        when(roadPosition.getUpcomingNode()).thenReturn(upcoming);

        when(previous.getId()).thenReturn("21487167");
        when(upcoming.getId()).thenReturn("272365223");

        //RUN
        final LazyLoadingRoadPosition refinedRoadPosition = new LazyLoadingRoadPosition(roadPosition, database);

        //ASSERT
        assertRefinedWay(refinedRoadPosition);
        assertRefinedStartJunction(refinedRoadPosition);
        assertRefinedEndJunction(refinedRoadPosition);
        assertRefinedRoadSegment(refinedRoadPosition);
        assertRefinedPrevious(refinedRoadPosition);
        assertRefinedUpcoming(refinedRoadPosition);

        assertEquals("4068038_251150126_428788319", refinedRoadPosition.getConnection().getId());
    }

    @Test
    public void onlyUpcomingNode_noDataRefined() {
        //SETUP
        when(roadPosition.getUpcomingNode()).thenReturn(upcoming);
        when(upcoming.getId()).thenReturn("272365223");

        //RUN
        final LazyLoadingRoadPosition refinedRoadPosition = new LazyLoadingRoadPosition(roadPosition, database);

        //ASSERT (no data available)
        assertNull(refinedRoadPosition.getConnection().getWay());
        assertNull(refinedRoadPosition.getConnection().getStartNode());
        assertNull(refinedRoadPosition.getConnection().getEndNode());
        assertNull(refinedRoadPosition.getConnection().getOutgoingConnections());
        assertNull(refinedRoadPosition.getConnection().getIncomingConnections());
        assertNull(refinedRoadPosition.getPreviousNode());

        assertEquals(0.0, refinedRoadPosition.getConnection().getLength(), 0.01d);
        assertEquals(13.37, refinedRoadPosition.getOffset(), 0.01d);

        assertRefinedUpcoming(refinedRoadPosition);

        assertEquals("?", refinedRoadPosition.getConnection().getId());
    }

    @Test
    public void unknownConnectionInput_noDataAvailable() {
        //SETUP
        when(roadPosition.getConnectionId()).thenReturn("thisABadConnectionId");

        //RUN
        final LazyLoadingRoadPosition refinedRoadPosition = new LazyLoadingRoadPosition(roadPosition, database);

        //ASSERT
        assertEquals("thisABadConnectionId", refinedRoadPosition.getConnection().getId());
        assertNull(refinedRoadPosition.getPreviousNode());
        assertNull(refinedRoadPosition.getUpcomingNode());
    }


    private void assertRefinedUpcoming(final LazyLoadingRoadPosition refinedRoadPosition) {
        assertEquals("272365223", refinedRoadPosition.getUpcomingNode().getId());
        assertEquals(52.512722, refinedRoadPosition.getUpcomingNode().getPosition().getLatitude(), 0.000001d);
        assertEquals(13.325340, refinedRoadPosition.getUpcomingNode().getPosition().getLongitude(), 0.000001d);
    }

    private void assertRefinedPrevious(final LazyLoadingRoadPosition refinedRoadPosition) {
        assertEquals("21487167", refinedRoadPosition.getPreviousNode().getId());
        assertEquals(52.512615, refinedRoadPosition.getPreviousNode().getPosition().getLatitude(), 0.000001d);
        assertEquals(13.3236446, refinedRoadPosition.getPreviousNode().getPosition().getLongitude(), 0.000001d);
    }

    private void assertRefinedRoadSegment(final LazyLoadingRoadPosition refinedRoadPosition) {
        assertEquals(222.06, refinedRoadPosition.getConnection().getLength(), 0.01d);
        assertEquals(13.37, refinedRoadPosition.getOffset(), 0.01d);
    }

    private void assertRefinedEndJunction(final LazyLoadingRoadPosition refinedRoadPosition) {
        assertEquals("428788319", refinedRoadPosition.getConnection().getEndNode().getId());
        assertEquals(52.512814, refinedRoadPosition.getConnection().getEndNode().getPosition().getLatitude(), 0.000001d);
        assertEquals(13.326735, refinedRoadPosition.getConnection().getEndNode().getPosition().getLongitude(), 0.000001d);
    }

    private void assertRefinedStartJunction(final LazyLoadingRoadPosition refinedRoadPosition) {
        assertEquals("251150126", refinedRoadPosition.getConnection().getStartNode().getId());
        assertEquals(52.5126, refinedRoadPosition.getConnection().getStartNode().getPosition().getLatitude(), 0.000001d);
        assertEquals(13.323472, refinedRoadPosition.getConnection().getStartNode().getPosition().getLongitude(), 0.000001d);
    }

    private void assertRefinedWay(final LazyLoadingRoadPosition refinedRoadPosition) {
        assertEquals("4068038", refinedRoadPosition.getConnection().getWay().getId());
        assertEquals(22.22d, refinedRoadPosition.getConnection().getWay().getMaxSpeedInMs(), 0.1d);
        assertEquals("primary", refinedRoadPosition.getConnection().getWay().getType());
    }

}
