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

import org.eclipse.mosaic.lib.enums.SpeedMode;

import java.util.BitSet;

public class SumoSpeedMode {

    /**
     * Converts speed mode meta information ({@link SpeedMode}) into specific {@link SumoSpeedMode}.
     *
     * @param speedMode Speed mode of the vehicle.
     * @return Specific sumo speed mode.
     */
    public static SumoSpeedMode translateFromEnum(final SpeedMode speedMode) {
        final SumoSpeedMode mode = new SumoSpeedMode();
        switch (speedMode) {
            case AGGRESSIVE:
                mode.setRegardMaximumAcceleration(true);
                mode.setRegardMaximumDeceleration(true);
                break;
            case NORMAL:
                mode.setRegardSafeSpeed(true);
                mode.setRegardMaximumAcceleration(true);
                mode.setRegardMaximumDeceleration(true);
                mode.setRegardRightOfWay(true);
                break;
            case CAUTIOUS:
                mode.setRegardSafeSpeed(true);
                mode.setRegardMaximumAcceleration(true);
                mode.setRegardMaximumDeceleration(true);
                mode.setRegardRightOfWay(true);
                mode.setBrakeHardToAvoidRedLight(true);
                break;
            case SPEEDER:
                mode.setRegardSafeSpeed(false);
                mode.setRegardMaximumAcceleration(true);
                mode.setRegardMaximumDeceleration(true);
                mode.setRegardRightOfWay(true);
                mode.setBrakeHardToAvoidRedLight(false);
                break;
            case DEFAULT:
                mode.setRegardSafeSpeed(true);
                mode.setRegardMaximumAcceleration(true);
                mode.setRegardMaximumDeceleration(true);
                mode.setRegardRightOfWay(true);
                mode.setBrakeHardToAvoidRedLight(true);
                break;
            default:
                throw new IllegalArgumentException("No translation available for speed behavior " + speedMode);
        }
        return mode;
    }

    private final BitSet bitset = new BitSet();

    /**
     * Regard maximum acceleration.
     *
     * @param regardMaximumAcceleration Boolean value indicating to accelerate.
     * @return Specific sumo speed mode for acceleration.
     */
    public SumoSpeedMode setRegardMaximumAcceleration(boolean regardMaximumAcceleration) {
        setOrUnset(1, regardMaximumAcceleration);
        return this;
    }

    /**
     * Regard maximum deceleration.
     *
     * @param regardMaximumDeceleration Boolean value indicating to decelerate.
     * @return Specific sumo speed mode for deceleration.
     */
    public SumoSpeedMode setRegardMaximumDeceleration(boolean regardMaximumDeceleration) {
        setOrUnset(2, regardMaximumDeceleration);
        return this;
    }

    /**
     * Regard right of way at intersections.
     *
     * @param regardRightOfWay Boolean value indicating to drive on the right side of the way.
     * @return Specific sumo speed mode for driving on the right side.
     */
    public SumoSpeedMode setRegardRightOfWay(boolean regardRightOfWay) {
        setOrUnset(3, regardRightOfWay);
        return this;
    }

    /**
     * Regard safe speed.
     *
     * @param regardSafeSpeed Boolean value indicating to enforce a safe speed.
     * @return Specific sumo speed mode for safe speed.
     */
    public SumoSpeedMode setRegardSafeSpeed(boolean regardSafeSpeed) {
        setOrUnset(0, regardSafeSpeed);
        return this;
    }

    /**
     * Brake hard to avoid passing a red light.
     *
     * @param brakeHardToAvoidRedLight Boolean value indicating to brake hard to avoid red light.
     * @return Specific sumo speed mode for braking hard.
     */
    public SumoSpeedMode setBrakeHardToAvoidRedLight(boolean brakeHardToAvoidRedLight) {
        setOrUnset(4, brakeHardToAvoidRedLight);
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
     * @return the integer bit set according to the set parameters
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
