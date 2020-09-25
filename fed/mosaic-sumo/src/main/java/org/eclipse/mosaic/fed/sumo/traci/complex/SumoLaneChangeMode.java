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

package org.eclipse.mosaic.fed.sumo.traci.complex;

import org.eclipse.mosaic.lib.enums.LaneChangeMode;

import java.util.BitSet;

/**
 * Class to set the LaneChangeMode via TraCI.
 *
 * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html#lane_change_mode_0xb6">Lane Change Mode</a>
 */
public class SumoLaneChangeMode {

    /**
     * Converts lane change meta information ({@link LaneChangeMode}) into specific {@link SumoLaneChangeMode}.
     *
     * @param laneChangeMode The meta {@link LaneChangeMode} to convert into {@link SumoLaneChangeMode}.
     * @return The Sumo lane change mode.
     */
    public static SumoLaneChangeMode translateFromEnum(final LaneChangeMode laneChangeMode) {
        final SumoLaneChangeMode mode = new SumoLaneChangeMode();
        mode.setSublaneChanges(true, false);
        switch (laneChangeMode) {
            case OFF:
                mode.setSublaneChanges(false, false);
                mode.setStrategicChanges(true, true);
                mode.setRespectOtherDrivers(RESPECT_SPEED_GAPS_OF_OTHER_DRIVERS_ADAPT_SPEED);
                break;
            // For strategic lane changes (change lanes to continue the route),
            // we always let SUMO overwrite requested lane changes (overrideTraci = true)
            case AGGRESSIVE:
                mode.setStrategicChanges(true, true);
                mode.setSpeedChanges(true, false);
                break;
            case CAUTIOUS:
                mode.setStrategicChanges(true, true);
                mode.setCooperativeChanges(true, false);
                mode.setSpeedChanges(true, false);
                mode.setRightDriveChanges(true, true);
                mode.setRespectOtherDrivers(RESPECT_SPEED_GAPS_OF_OTHER_DRIVERS_DO_NOT_ADAPT_SPEED);
                break;
            case PASSIVE:
                mode.setStrategicChanges(true, true);
                mode.setCooperativeChanges(true, false);
                mode.setSpeedChanges(true, false);
                mode.setRightDriveChanges(true, true);
                mode.setRespectOtherDrivers(RESPECT_SPEED_GAPS_OF_OTHER_DRIVERS_ADAPT_SPEED);
                break;
            case COOPERATIVE:
            case DEFAULT:
                mode.setStrategicChanges(true, true);
                mode.setCooperativeChanges(true, false);
                mode.setSpeedChanges(true, false);
                mode.setRightDriveChanges(true, false);
                mode.setRespectOtherDrivers(RESPECT_SPEED_GAPS_OF_OTHER_DRIVERS_ADAPT_SPEED);
                break;
            default:
                throw new IllegalArgumentException("No translation available for lane change behavior " + laneChangeMode);
        }
        return mode;
    }

    /**
     * Driving mode indicate the driving style.
     */
    public static final int DO_NOT_RESPECT_OTHER_DRIVERS = 0;
    public static final int AVOID_COLLISIONS_WITH_OTHER_DRIVERS = 1;
    public static final int RESPECT_SPEED_GAPS_OF_OTHER_DRIVERS_ADAPT_SPEED = 2;
    public static final int RESPECT_SPEED_GAPS_OF_OTHER_DRIVERS_DO_NOT_ADAPT_SPEED = 3;

    private final BitSet bitset = new BitSet();

    /**
     * Set the mode how to respect other drivers during lane changes.
     *
     * @param respectOtherDrivers The number indicates the level of the driving style.
     * @return The level of the driving style.
     */
    public SumoLaneChangeMode setRespectOtherDrivers(int respectOtherDrivers) {
        switch (respectOtherDrivers) {
            case DO_NOT_RESPECT_OTHER_DRIVERS:
                setOrUnset(8, false);
                setOrUnset(9, false);
                break;
            case AVOID_COLLISIONS_WITH_OTHER_DRIVERS:
                setOrUnset(8, true);
                setOrUnset(9, false);
                break;
            case RESPECT_SPEED_GAPS_OF_OTHER_DRIVERS_ADAPT_SPEED:
                setOrUnset(8, false);
                setOrUnset(9, true);
                break;
            case RESPECT_SPEED_GAPS_OF_OTHER_DRIVERS_DO_NOT_ADAPT_SPEED:
                setOrUnset(8, true);
                setOrUnset(9, true);
                break;
            default:
                throw new IllegalArgumentException("");
        }
        return this;
    }

    /**
     * Do speed gain changes.
     *
     * @param speedChanges  Boolean value to change the speed.
     * @param overrideTraci Boolean value to override the previous Traci command.
     * @return this
     */
    public SumoLaneChangeMode setSpeedChanges(boolean speedChanges, boolean overrideTraci) {
        setOrUnset(overrideTraci ? 5 : 4, speedChanges);
        setOrUnset(overrideTraci ? 4 : 5, false);
        return this;
    }

    /**
     * Do strategic lane changes.
     *
     * @param strategicChanges Boolean value to change the speed.
     * @param overrideTraci    Boolean value to override the previous Traci command.
     * @return this
     */
    public SumoLaneChangeMode setStrategicChanges(boolean strategicChanges, boolean overrideTraci) {
        setOrUnset(overrideTraci ? 1 : 0, strategicChanges);
        setOrUnset(overrideTraci ? 0 : 1, false);
        return this;
    }

    /**
     * Do cooperative lane changes.
     *
     * @param cooperativeChanges Boolean value to change the speed.
     * @param overrideTraci      Boolean value to override the previous Traci command.
     * @return this
     */
    public SumoLaneChangeMode setCooperativeChanges(boolean cooperativeChanges, boolean overrideTraci) {
        setOrUnset(overrideTraci ? 3 : 2, cooperativeChanges);
        setOrUnset(overrideTraci ? 2 : 3, false);
        return this;
    }

    /**
     * Do right drive changes.
     *
     * @param rightDriveChanges Boolean value to change the speed.
     * @param overrideTraci     Boolean value to override the previous Traci command.
     * @return this
     */
    public SumoLaneChangeMode setRightDriveChanges(boolean rightDriveChanges, boolean overrideTraci) {
        setOrUnset(overrideTraci ? 7 : 6, rightDriveChanges);
        setOrUnset(overrideTraci ? 6 : 7, false);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public SumoLaneChangeMode setSublaneChanges(boolean sublaneChanges, boolean overrideTraci) {
        setOrUnset(overrideTraci ? 11 : 10, sublaneChanges);
        setOrUnset(overrideTraci ? 10 : 11, false);
        return this;
    }

    private void setOrUnset(int index, boolean value) {
        if (value) {
            bitset.set(index);
        } else {
            bitset.clear(index);
        }
    }

    /**
     * Returns the integer bit set according to the set parameters.
     *
     * @return the integer bit set according to the set parameters.
     */
    public final int getAsInteger() {
        int result = 0;
        for (int i = 0; i < bitset.length(); i++) {
            if (bitset.get(i)) {
                result += Math.pow(2, i);
            }
        }
        return result;
    }
}
