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
 */

package org.eclipse.mosaic.lib.objects;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.Comparator;

/**
 * Comparator to sort simulation units the derived objects as
 * vehicles, rsu and so on by alphanumeric order (natural order).
 */
@SuppressWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE", justification = "We don't serialize this.")
public class UnitNameComparator implements Comparator<UnitData> {

    @Override
    public int compare(UnitData ud1, UnitData ud2) {
        if (ud1.getName() == null && ud2.getName() == null) {
            return 0;
        }
        if (ud1.getName() == null) {
            return 1;
        }
        if (ud2.getName() == null) {
            return -1;
        }

        String[] ud1Parts = ud1.getName().split("_", 2);
        String[] ud2Parts = ud2.getName().split("_", 2);
        if (!ud1Parts[0].equalsIgnoreCase(ud2Parts[0])) {
            return ud1.getName().compareTo(ud2.getName());
        } else {
            return Integer.valueOf(ud1Parts[1]).compareTo(Integer.valueOf(ud2Parts[1]));
        }
    }
}
