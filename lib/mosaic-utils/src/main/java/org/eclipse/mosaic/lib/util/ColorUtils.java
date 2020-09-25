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

package org.eclipse.mosaic.lib.util;

import java.awt.Color;

public class ColorUtils {

    private ColorUtils() {
        // static methods only
    }

    /**
     * Creates a {@link Color} from a string.
     *
     * @param colorString  color string representation (#XXX, #XXXXXX or the name of the color like red, green etc.)
     *
     * @return the color, {@code null} if no color matches to the given color string
     */
    public static Color toColor(String colorString) {
        return toColor(colorString, null);
    }

    /**
     * Creates a {@link Color} from a string.
     *
     * @param colorString  color string representation (#XXX, #XXXXXX or the name of the color like red, green etc.)
     * @param defaultColor the fallback color (if the text to Color transformation didn't work)
     * @return the color
     */
    public static Color toColor(String colorString, Color defaultColor) {
        if (colorString == null) {
            return defaultColor;
        }
        try {
            if (colorString.startsWith("#") && colorString.length() == 4) {
                return new Color(
                        Integer.parseInt(colorString.substring(1, 2), 16) * 16,
                        Integer.parseInt(colorString.substring(2, 3), 16) * 16,
                        Integer.parseInt(colorString.substring(3, 4), 16) * 16
                );
            }
            if (colorString.startsWith("#") && colorString.length() == 7) {
                return new Color(
                        Integer.parseInt(colorString.substring(1, 3), 16),
                        Integer.parseInt(colorString.substring(3, 5), 16),
                        Integer.parseInt(colorString.substring(5, 7), 16)
                );
            }
            return (Color) Color.class.getField(colorString.toLowerCase()).get(null);
        } catch (Exception e) {
            return defaultColor;
        }
    }
}
