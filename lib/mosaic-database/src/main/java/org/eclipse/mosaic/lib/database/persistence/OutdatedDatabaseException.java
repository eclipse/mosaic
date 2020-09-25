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

package org.eclipse.mosaic.lib.database.persistence;

/**
 * Indicates that the scenario database is outdated and needs to be converted with the
 * tool scenario-convert.
 */
public class OutdatedDatabaseException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>OutdatedDatabaseException</code> without detail message.
     */
    public OutdatedDatabaseException() {
        super("outdated database version detected, needs to be converted with scenario-convert");
    }

    /**
     * Constructs an instance of <code>OutdatedDatabaseException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public OutdatedDatabaseException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>OutdatedDatabaseException</code> with the specified detail
     * message and root Exception.
     *
     * @param msg the detail message.
     * @param e   Exception indicating outdated database.
     */
    public OutdatedDatabaseException(String msg, Exception e) {
        super(msg, e);
    }
}
