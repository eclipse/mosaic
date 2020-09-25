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

package org.eclipse.mosaic.lib.math;

public class SpeedUtils {

    /**
     * Converts a speed value from km/h to m/s.
     *
     * @param kmh Speed in km/h
     * @return Corresponding speed in m/s
     */
    public static double kmh2ms(double kmh) {
        return kmh / 3.6;
    }

    /**
     * Converts a speed value from m/s to km/h.
     *
     * @param ms Speed in m/s
     * @return Corresponding speed in km/h
     */
    public static double ms2kmh(double ms) {
        return ms * 3.6;
    }

    /**
     * computes the braking distance
     *
     * @param velocity     velocity in [m/s]
     * @param acceleration acceleration for braking[m/s^2]
     * @return
     */
    public static double computeBrakeDistance(double velocity, double acceleration) {
        return computeDistanceForSpeedChange(velocity, 0.0, -Math.abs(acceleration));
    }

    /**
     * Computes the distance needed to change the speed according to given params. Sign of acceleration is considered,
     * i.e. for braking (desiredSpeed < initial speed) acceleration has to be negative.
     *
     * @param initialSpeed start velocity in [m/s]
     * @param desiredSpeed final velocity in [m/s]
     * @param a            acceleration during speed change [m/s^2]
     * @return resulting distance in [m]
     */
    public static double computeDistanceForSpeedChange(double initialSpeed, double desiredSpeed, double a) {
        double t = computeTimeForSpeedChange(initialSpeed, desiredSpeed, a);
        return initialSpeed * t + a * 0.5 * t * t;
    }

    public static double computeTimeForSpeedChange(double initialSpeed, double desiredSpeed, double a) {
        return (desiredSpeed - initialSpeed) / a;
    }

    /**
     * @param initialSpeed start velocity in [m/s]
     * @param desiredSpeed final velocity in [m/s]
     * @param x            available track length [m]
     * @return needed acceleration in [m/s^2] to achieve the speed change on available track length
     */
    public static double computeAccelerationForSpeedChange(double initialSpeed, double desiredSpeed, double x) {
        double vDif = desiredSpeed - initialSpeed;
        double a = (vDif * vDif + desiredSpeed * vDif) / (2.0 * x);
        if (vDif == 0) {
            return 0;
        } else if (vDif < 0) {
            // fixme: this doesn't seem right...
            return -a;
        } else {
            return a / 2;
        }
    }

    /**
     * Computes the maximum speed for a curve with given curvature and specified lateral acceleration. Returned speed
     * might be {@link Double#POSITIVE_INFINITY} in case the curvature is 0.0 (straight).
     *
     * @param lateralAccel lateral acceleration in [m/s]
     * @param curvature    curvature in [1/m]
     * @return speed to achieve given lateral acceleration in curve
     */
    public static double computeMaxCurveSpeed(double lateralAccel, double curvature) {
        return Math.sqrt(Math.abs(lateralAccel / curvature));
    }

}
