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

package org.eclipse.mosaic.lib.database.road;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * This represents a turn restriction. And can either say that one cannot turn from
 * {@link #getSource()} to {link #getTarget} through {@link #getVia} or that one can only
 * turn/go from {@link #getSource()} to {link #getTarget} through {@link #getVia}.
 */
public class Restriction {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public enum Type {
        Only, Not;

        public static Type convertTypeFromString(String typeString) {
            if (typeString.equals(String.valueOf(Type.Not))) {
                return Type.Not;
            }
            if (typeString.equals(String.valueOf(Type.Only))) {
                return Type.Only;
            }
            return null;
        }

    }

    private final String id;
    private final Type type;
    private final Way source;
    private final Node via;
    private final Way target;

    /**
     * Creates a new {@link Restriction} object.
     *
     * @param id     Unique identifier of the restriction.
     * @param type   The type of the restriction.
     * @param source Start point the restriction.
     * @param target Target of the restriction.
     * @param via    Restriction over the node.
     */
    public Restriction(@Nonnull String id, @Nonnull Type type, @Nonnull Way source, @Nonnull Node via, @Nonnull Way target) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.source = Objects.requireNonNull(source);
        this.via = Objects.requireNonNull(via);
        this.target = Objects.requireNonNull(target);

        // make sure this was created safely
        if (this.id.isEmpty()) {
            throw new RuntimeException("A network restriction from scenario database has not been properly initialized!");
        }
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public Type getType() {
        return type;
    }

    @Nonnull
    public Way getSource() {
        return source;
    }

    @Nonnull
    public Way getTarget() {
        return target;
    }

    @Nonnull
    public Node getVia() {
        return via;
    }

    /**
     * This tries to apply this {@link Restriction} on the affected {@link Connection}s.
     */
    public void applyRestriction() {
        // the actual connections affected
        Connection from = null;
        Connection to = null;

        // find input connection
        boolean found = false;
        for (Connection connection : via.getIncomingConnections()) {
            if (connection.getWay().equals(source)) {
                if (found) {
                    // way found in multiple incoming connections,
                    // this means we do not end here (which is a requirement from OSM)!
                    String error = "the 'from' way ({}) referenced in this restriction does "
                            + "not seem to start or end at the 'via' node ({}), "
                            + "please check OSM input "
                            + "(see http://wiki.openstreetmap.org/wiki/Relation:restriction#cite_note-waysplit-2)";
                    log.error(error, source.getId(), via.getId());
                    return;
                } else {
                    from = connection;
                    found = true;
                }
            }
        }

        // find output connection
        found = false;
        for (Connection connection : via.getOutgoingConnections()) {
            if (connection.getWay().equals(target)) {
                if (found) {
                    // way found in multiple incoming connections,
                    // this means we do not start here (which is a requirement from OSM)!
                    String error = "the 'to' way ({}) referenced in this restriction does "
                            + "not seem to start or end at the 'via' node ({}), "
                            + "please check OSM input "
                            + "(see http://wiki.openstreetmap.org/wiki/Relation:restriction#cite_note-waysplit-2)";
                    log.error(error, target.getId(), via.getId());
                    return;
                } else {
                    to = connection;
                    found = true;
                }
            }
        }

        // last but not least actually filter
        if (from != null && to != null) {
            from.applyTurnRestriction(type, to);
        }
    }

}
