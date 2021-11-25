/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.math;

public enum MatrixAlignment {

    /**
     * Values are stored in a way, that subsequent values belong
     * to the same row. That is, in a 3x3 matrix, the first 3 values
     * belong to the first row, the second 3 values to the second row, and so on.
     */
    ROWS,
    /**
     * Values are stored in a way, that subsequent values
     * to the same column. That is, in a 3x3 matrix, the first 3 values
     * belong to the first column, the second 3 values to the second column, and so on.
     */
    COLUMNS
}
