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

import org.eclipse.mosaic.lib.database.Database;

/**
 * This interface regulates how the database accesses its persistence.
 */
public interface DatabaseLoader {

    /**
     * Loads data from the given file into the given database.
     *
     * @param filename Name of the database file.
     * @return Loaded database.
     * @throws OutdatedDatabaseException when version information is not found or is really outdated.
     */
    Database.Builder loadFromFile(String filename) throws OutdatedDatabaseException;

}
