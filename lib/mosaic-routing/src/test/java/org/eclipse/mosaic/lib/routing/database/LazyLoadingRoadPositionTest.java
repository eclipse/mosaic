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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.objects.road.IConnection;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.road.IWay;

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

@RunWith(MockitoJUnitRunner.class)
public class LazyLoadingRoadPositionTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final static String dbFile = "/tiergarten.db";

    private Database database;

    @Mock
    IWay way;
    @Mock
    INode connectionStart;
    @Mock
    INode connectionEnd;
    @Mock
    INode previous;
    @Mock
    INode upcoming;
    @Mock
    IConnection connection;
    @Mock
    IRoadPosition roadPosition;

    @Before
    public void setup() throws IOException {
        final File dbFileCopy = folder.newFile("tiergarten.db");

        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream(dbFile), dbFileCopy);

        database = Database.loadFromFile(dbFileCopy);

        when(roadPosition.getConnection()).thenReturn(connection);
        when(roadPosition.getPreviousNode()).thenReturn(previous);
        when(roadPosition.getUpcomingNode()).thenReturn(upcoming);
        when(roadPosition.getOffset()).thenReturn(13.37d);
        when(connection.getStartNode()).thenReturn(connectionStart);
        when(connection.getEndNode()).thenReturn(connectionEnd);
        when(connection.getWay()).thenReturn(way);
    }

    @Test
    public void sumoAvailableValues_everythingAvailable() {
        //SETUP
        when(way.getId()).thenReturn("4068038");
        when(connectionStart.getId()).thenReturn("251150126");
        when(connectionEnd.getId()).thenReturn("428788319");
        when(previous.getId()).thenReturn("21487167");
        when(roadPosition.getUpcomingNode()).thenReturn(null);

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
    public void phabmacsAvailableValues_everythingAvailable() {
        //SETUP
        when(way.getId()).thenReturn("4068038");
        when(connection.getStartNode()).thenReturn(null);
        when(connection.getEndNode()).thenReturn(null);
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
    public void onlyPrevAndUpcoming_everythingAvailable() {
        //SETUP
        when(connection.getWay()).thenReturn(null);
        when(connection.getStartNode()).thenReturn(null);
        when(connection.getEndNode()).thenReturn(null);
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
        when(connection.getWay()).thenReturn(null);
        when(connection.getStartNode()).thenReturn(null);
        when(connection.getEndNode()).thenReturn(null);
        when(roadPosition.getPreviousNode()).thenReturn(null);
        when(upcoming.getId()).thenReturn("272365223");

        //RUN
        final LazyLoadingRoadPosition refinedRoadPosition = new LazyLoadingRoadPosition(roadPosition, database);

        //ASSERT (no data available)
        assertNull(refinedRoadPosition.getConnection().getWay());
        assertNull(refinedRoadPosition.getConnection().getStartNode());
        assertNull(refinedRoadPosition.getConnection().getEndNode());
        assertNull(refinedRoadPosition.getPreviousNode());

        assertEquals(0.0, refinedRoadPosition.getConnection().getLength(), 0.01d);
        assertEquals(13.37, refinedRoadPosition.getOffset(), 0.01d);

        assertRefinedUpcoming(refinedRoadPosition);

        assertEquals("?_?_?", refinedRoadPosition.getConnection().getId());
    }

    @Test
    public void unknownWayInput_noRoadSegmentDataAvailable() {
        //SETUP
        when(way.getId()).thenReturn("4068038x");
        when(connectionStart.getId()).thenReturn("251150126");
        when(connectionEnd.getId()).thenReturn("428788319");
        when(previous.getId()).thenReturn("21487167");
        when(roadPosition.getUpcomingNode()).thenReturn(null);

        //RUN
        final LazyLoadingRoadPosition refinedRoadPosition = new LazyLoadingRoadPosition(roadPosition, database);

        //ASSERT
        assertRefinedStartJunction(refinedRoadPosition);
        assertRefinedEndJunction(refinedRoadPosition);
        assertRefinedPrevious(refinedRoadPosition);

        assertNull(refinedRoadPosition.getUpcomingNode());
        assertEquals(0.0, refinedRoadPosition.getConnection().getLength(), 0.01d);

        assertEquals("4068038x_251150126_428788319", refinedRoadPosition.getConnection().getId());
    }

    @Test
    public void unknownConnectionStartInput_noRoadSegmentDataAvailable() {
        //SETUP
        when(way.getId()).thenReturn("4068038");
        when(connectionStart.getId()).thenReturn("251150126x");
        when(connectionEnd.getId()).thenReturn("428788319");
        when(previous.getId()).thenReturn("21487167");
        when(roadPosition.getUpcomingNode()).thenReturn(null);

        //RUN
        final LazyLoadingRoadPosition refinedRoadPosition = new LazyLoadingRoadPosition(roadPosition, database);

        //ASSERT
        assertRefinedWay(refinedRoadPosition);
        assertRefinedEndJunction(refinedRoadPosition);
        assertRefinedPrevious(refinedRoadPosition);

        assertEquals("251150126x", refinedRoadPosition.getConnection().getStartNode().getId());
        assertNull(refinedRoadPosition.getConnection().getStartNode().getPosition());

        assertNull(refinedRoadPosition.getUpcomingNode());
        assertEquals(0.0, refinedRoadPosition.getConnection().getLength(), 0.01d);

        assertEquals("4068038_251150126x_428788319", refinedRoadPosition.getConnection().getId());
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
