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

package org.eclipse.mosaic.app.tutorial.interunitcommunication;

import org.eclipse.mosaic.interactions.application.ApplicationInteraction;

import java.util.Objects;

/**
 * Example interaction.
 */
public class MyInteraction extends ApplicationInteraction {

    /**
     * The content of the interaction.
     */
    private final String content;

    public MyInteraction(long time, String unitId, String content) {
        super(time, unitId);
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.content);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MyInteraction other = (MyInteraction) obj;
        return Objects.equals(this.content, other.content);
    }

    @Override
    public String toString() {
        return "MyInteraction{" + "content=" + this.content + '}';
    }

}
