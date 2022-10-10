package org.eclipse.mosaic.rti.api.parameters;

import static org.eclipse.mosaic.rti.api.parameters.FederatePriority.HIGHEST;
import static org.eclipse.mosaic.rti.api.parameters.FederatePriority.LOWEST;
import static org.eclipse.mosaic.rti.api.parameters.FederatePriority.compareTo;
import static org.eclipse.mosaic.rti.api.parameters.FederatePriority.higher;
import static org.eclipse.mosaic.rti.api.parameters.FederatePriority.lower;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FederatePriorityTest {

    @Test
    public void testCompareTo() {
        assertTrue(compareTo(HIGHEST, LOWEST) > 0);
        assertTrue(compareTo(LOWEST, HIGHEST) < 0);
        assertEquals(0, compareTo(LOWEST, LOWEST));
    }

    @Test
    public void testHigher() {
        byte priority = 23;
        assertTrue(compareTo(higher(priority), priority) > 0);

        // check if HIGHEST is not exceeded
        assertEquals(0, compareTo(higher(HIGHEST), HIGHEST));
    }

    @Test
    public void testLower() {
        byte priority = 23;
        assertTrue(compareTo(lower(priority), priority) < 0);

        // check if LOWEST is not exceeded
        assertEquals(0, compareTo(lower(LOWEST), LOWEST));
    }

}