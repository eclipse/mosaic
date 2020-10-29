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

package org.eclipse.mosaic.lib.objects.mapping;

import java.util.List;

/**
 * A Server simulation unit that is equipped with applications.
 */
public final class ServerMapping extends UnitMapping {

    private static final long serialVersionUID = 1L;

    /**
     * Contains configuration of a server. The mapping is fairly simple
     * @param name name of the unit
     * @param group group that the unit belongs to
     * @param applications a list of applications to be mapped onto the unit
     */
    public ServerMapping(final String name, final String group, final List<String> applications) {
        super(name, group, applications);
    }
}
