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

package org.eclipse.mosaic.lib.objects.communication;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;

/**
 * This class represents the configuration of a vehicles cellular communication
 * interface.
 */
@Immutable
public class CellCommunicationConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nodeID;

    private final boolean enabled;

    public CellCommunicationConfiguration(String nodeID, boolean enabled) {
        this.nodeID = nodeID;
        this.enabled = enabled;
    }

    public final boolean isCellCommunicationEnabled() {
        return enabled;
    }

    public final String getNodeID() {
        return nodeID;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 83)
                .append(nodeID)
                .append(isCellCommunicationEnabled())
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        CellCommunicationConfiguration other = (CellCommunicationConfiguration) obj;
        return new EqualsBuilder()
                .append(this.nodeID, other.nodeID)
                .append(this.isCellCommunicationEnabled(), other.isCellCommunicationEnabled())
                .isEquals();
    }

}
