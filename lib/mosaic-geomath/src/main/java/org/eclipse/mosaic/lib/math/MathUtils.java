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

public class MathUtils {

    // values below epsilon constants will be considered as zero by isZero() functions
    public static final double EPSILON_D = 1e-8;
    public static final float EPSILON_F = 1e-5f;

    public static boolean isFuzzyZero(float a) {
        return Math.abs(a) < EPSILON_F;
    }

    public static boolean isFuzzyZero(double a) {
        return Math.abs(a) < EPSILON_D;
    }

    public static boolean isFuzzyEqual(float a, float b) {
        if (Math.abs(a) <= EPSILON_F && Math.abs(b) <= EPSILON_F) {
            return true;
        } else {
            return Math.abs(a - b) <= Math.abs(Math.max(a, b)) * EPSILON_F;
        }
    }

    public static boolean isFuzzyEqual(double a, double b) {
        if (Math.abs(a) <= EPSILON_D && Math.abs(b) <= EPSILON_D) {
            return true;
        } else {
            return Math.abs(a - b) <= Math.abs(Math.max(a, b)) * EPSILON_D;
        }
    }

    public static float clamp(float d, float min, float max) {
        if (d > max) {
            return max;
        } else if (d < min) {
            return min;
        }
        return d;
    }

    public static double clamp(double d, double min, double max) {
        if (d > max) {
            return max;
        } else if (d < min) {
            return min;
        }
        return d;
    }

    public static int clamp(int i, int min, int max) {
        if (i > max) {
            return max;
        } else if (i < min) {
            return min;
        }
        return i;
    }

    public static float wrapAnglePiPi(float a) {
        return (float) wrapAnglePiPi((double) a);
    }

    public static double wrapAnglePiPi(double a) {
        if (a > Math.PI) {
            int mul = (int) (a / Math.PI) + 1;
            a -= Math.PI * mul;
        } else if (a < -Math.PI) {
            int mul = (int) (a / Math.PI) - 1;
            a -= Math.PI * mul;
        }
        return a;
    }

    public static double angleDif(double a, double b) {
        double dif = b - a;
        if (dif > Math.PI) {
            return ((dif + Math.PI) % (Math.PI * 2)) - Math.PI;
        } else if (dif < -Math.PI) {
            return ((dif - Math.PI) % (Math.PI * 2)) + Math.PI;
        } else {
            return dif;
        }
    }

    public static double angleDifDeg(double a, double b) {
        double dif = b - a;
        if (dif > 180) {
            return ((dif + 180) % 360) - 180;
        } else if (dif < -180) {
            return ((dif - 180) % 360) + 180;
        } else {
            return dif;
        }
    }

    public static double normalizeDegree(double deg) {
        return (deg + 360) % 360;
    }

    public static float max(float a, float b, float c) {
        if (a > b && a > c) {
            return a;
        } else if (b > c) {
            return b;
        } else {
            return c;
        }
    }

    public static float min(float a, float b, float c) {
        if (a < b && a < c) {
            return a;
        } else if (b < c) {
            return b;
        } else {
            return c;
        }
    }

    public static double max(double a, double b, double c) {
        if (a > b && a > c) {
            return a;
        } else if (b > c) {
            return b;
        } else {
            return c;
        }
    }

    public static double min(double a, double b, double c) {
        if (a < b && a < c) {
            return a;
        } else if (b < c) {
            return b;
        } else {
            return c;
        }
    }

    /**
     * Returns the greatest common divisor of a and b (e.g. {@code gcd(12, 18) = 6})
     * @param a first number
     * @param b second number
     * @return greatest common divisor of a and b.
     */
    public static int gcd(int a, int b) {
        if (b == 0) {
            return a;
        } else {
            return gcd(b, a % b);
        }
    }

}