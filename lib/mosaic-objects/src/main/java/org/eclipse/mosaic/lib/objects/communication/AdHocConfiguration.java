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

package org.eclipse.mosaic.lib.objects.communication;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * This class represents the configuration of a vehicles Ad-Hoc interface.
 * The vehicle shall be configured to have zero, one or two interfaces with the according
 * configuration specified in the respective InterfaceConfigurations.
 */
@Immutable
public class AdHocConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nodeId;

    private final List<InterfaceConfiguration> interfaces = new ArrayList<>(2);

    /**
     * Creates a configuration with one activated radio.
     *
     * @param nodeId the node who's radio is to be configured
     */
    private AdHocConfiguration(String nodeId, List<InterfaceConfiguration> interfaceConfigurations) {
        this.nodeId = nodeId;
        this.interfaces.addAll(interfaceConfigurations);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 97)
                .append(nodeId)
                .append(interfaces)
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

        AdHocConfiguration other = (AdHocConfiguration) obj;
        return new EqualsBuilder()
                .append(this.nodeId, other.nodeId)
                .append(this.interfaces, other.interfaces)
                .isEquals();
    }

    /**
     * Returns a string representation of the configuration.
     */
    @Override
    public String toString() {
        return "Configure node: " + getNodeId() + " with " + getRadioMode().name() + " radio.\n";
    }

    /**
     * Returns get the number of installed radios.
     */
    public RadioMode getRadioMode() {
        if (interfaces.size() == 0) {
            return RadioMode.OFF;
        } else if (interfaces.size() == 1) {
            return RadioMode.SINGLE;
        }
        return RadioMode.DUAL;
    }

    /**
     * Returns the nodeId.
     */
    public String getNodeId() {
        return this.nodeId;
    }

    /**
     * Returns the configuration for the first interface.
     */
    public InterfaceConfiguration getConf0() {
        return Iterables.getFirst(interfaces, null);
    }

    /**
     * Returns the configuration for the second interface.
     */
    public InterfaceConfiguration getConf1() {
        return Iterables.get(interfaces, 1, null);
    }

    /**
     * This enum describes the number of radios and thus the multi radio mode.
     */
    public enum RadioMode {
        OFF, SINGLE, DUAL
    }

    public static class Builder {
        private String nodeId;
        private List<InterfaceConfiguration> interfaceConfigurations = new ArrayList<>();

        public Builder(String nodeId) {
            this.nodeId = nodeId;
        }

        public Builder addInterface(InterfaceConfiguration interfaceConfiguration) {
            interfaceConfigurations.add(interfaceConfiguration);
            return this;
        }

        public AdHocConfiguration create() {
            return new AdHocConfiguration(nodeId, interfaceConfigurations);
        }

    }
}
