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

package org.eclipse.mosaic.lib.routing.database;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Way;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.road.IWay;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;

/**
 * <p>Provides information about the way of a {@link IRoadPosition}. Any missing information is gathered lazy by requesting the
 * scenario-database as soon as the respective getter method is called. Those information is cached for later calls of the same method by
 * storing the way from the scenario-database.</p>
 *
 * <p>The id of the way <b>must</b> be known in order to retrieve more information such as type or maximum speed.</p>
 */
class LazyLoadingWay implements IWay {

    private static final long serialVersionUID = 1L;

    /* This reference must be kept transient, since it should never be serialized (e.g. by GSON) */
    @Nullable
    private final transient Database database;

    /* This reference must be kept transient, since it should never be serialized (e.g. by GSON) */
    @SuppressWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    @Nullable
    private transient Way scenarioDatabaseWay;

    private final IWay currentWay;

    LazyLoadingWay(Way scenarioDatabaseWay) {
        this.scenarioDatabaseWay = scenarioDatabaseWay;
        this.database = null;
        this.currentWay = null;
    }

    LazyLoadingWay(IWay currentWay, Database database) {
        this.currentWay = currentWay;
        this.database = database;
    }

    @Override
    public String getId() {
        return scenarioDatabaseWay != null ? scenarioDatabaseWay.getId() : currentWay.getId();
    }

    @Override
    public String getType() {
        if (scenarioDatabaseWay == null && currentWay.getType() == null) {
            scenarioDatabaseWay = database.getWay(getId());
        }
        return scenarioDatabaseWay != null ? scenarioDatabaseWay.getType() : currentWay.getType();
    }

    @Override
    public double getMaxSpeedInMs() {
        if (scenarioDatabaseWay == null) {
            scenarioDatabaseWay = database.getWay(getId());
        }
        return scenarioDatabaseWay != null ? scenarioDatabaseWay.getMaxSpeedInMs() : currentWay.getMaxSpeedInMs();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 23)
                .append(this.currentWay)
                .append(this.scenarioDatabaseWay)
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

        LazyLoadingWay sdw = (LazyLoadingWay) obj;
        return new EqualsBuilder()
                .append(this.currentWay, sdw.currentWay)
                .append(this.scenarioDatabaseWay, sdw.scenarioDatabaseWay)
                .isEquals();
    }

}
