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

package org.eclipse.mosaic.lib.spatial;

import org.eclipse.mosaic.lib.math.MathUtils;
import org.eclipse.mosaic.lib.math.Matrix3d;
import org.eclipse.mosaic.lib.math.MatrixAlignment;
import org.eclipse.mosaic.lib.math.Vector3d;

public class RotationMatrix extends Matrix3d {


    private static final RotationMatrix tmpRotA = new RotationMatrix();
    private static final RotationMatrix tmpRotB = new RotationMatrix();

    /**
     * Creates a new transformation matrix. The initial values match the identity matrix.
     */
    public RotationMatrix() {
        setIdentity();
    }

    /**
     * Creates a new transformation matrix with values from the given TransformationMatrix.
     */
    public RotationMatrix(RotationMatrix copyFrom) {
        set(copyFrom);
    }

    /**
     * Adds rotation to this transformation matrix.
     */
    public RotationMatrix rotate(double angleDeg, Vector3d axis) {
        return rotate(angleDeg, axis.x, axis.y, axis.z);
    }

    /**
     * Adds rotation to this transformation matrix.
     */
    public RotationMatrix rotate(double angleDeg, double axX, double axY, double axZ) {
        synchronized (tmpRotA) {
            tmpRotA.setRotation(angleDeg, axX, axY, axZ);
            set(multiply(tmpRotA, tmpRotB));
        }
        return this;
    }

    private void setRotation(double angleDeg, double axX, double axY, double axZ) {
        double a = Math.toRadians(angleDeg);
        double x = axX;
        double y = axY;
        double z = axZ;
        double s = Math.sin(a);
        double c = Math.cos(a);
        if (1.0f == x && 0.0f == y && 0.0f == z) {
            m[4] = c;
            m[8] = c;
            m[5] = s;
            m[7] = -s;
            m[1] = 0f;
            m[2] = 0f;
            m[3] = 0f;
            m[6] = 0f;
            m[0] = 1f;
        } else if (0.0f == x && 1.0f == y && 0.0f == z) {
            m[0] = c;
            m[8] = c;
            m[6] = s;
            m[2] = -s;
            m[1] = 0f;
            m[3] = 0f;
            m[5] = 0f;
            m[7] = 0f;
            m[4] = 1f;
        } else if (0.0f == x && 0.0f == y && 1.0f == z) {
            m[0] = c;
            m[4] = c;
            m[1] = s;
            m[3] = -s;
            m[2] = 0f;
            m[5] = 0f;
            m[6] = 0f;
            m[7] = 0f;
            m[8] = 1f;
        } else {
            double len = Math.sqrt(x * x + y * y + z * z);
            if (!MathUtils.isFuzzyEqual(len, 1f)) {
                double recipLen = 1.0 / len;
                x *= recipLen;
                y *= recipLen;
                z *= recipLen;
            }
            double nc = 1.0f - c;
            double xy = x * y;
            double yz = y * z;
            double zx = z * x;
            double xs = x * s;
            double ys = y * s;
            double zs = z * s;
            m[0] = x * x * nc + c;
            m[3] = xy * nc - zs;
            m[6] = zx * nc + ys;
            m[1] = xy * nc + zs;
            m[4] = y * y * nc + c;
            m[7] = yz * nc - xs;
            m[2] = zx * nc - ys;
            m[5] = yz * nc + xs;
            m[8] = z * z * nc + c;
        }
    }

    @Override
    public RotationMatrix copy() {
        return new RotationMatrix(this);
    }

    @Override
    public RotationMatrix set(Matrix3d mat) {
        return (RotationMatrix) super.set(mat);
    }

    @Override
    public RotationMatrix setIdentity() {
        return (RotationMatrix) super.setIdentity();
    }

    @Override
    public RotationMatrix setZero() {
        return (RotationMatrix) super.setZero();
    }

    @Override
    public RotationMatrix set(double[] values, MatrixAlignment alignment) {
        return (RotationMatrix) super.set(values, alignment);
    }

    @Override
    public RotationMatrix set(float[] values, MatrixAlignment alignment) {
        return (RotationMatrix) super.set(values, alignment);
    }

    @Override
    public RotationMatrix set(int row, int col, double value) {
        return (RotationMatrix) super.set(row, col, value);
    }

    @Override
    public RotationMatrix transpose() {
        return (RotationMatrix) super.transpose();
    }

    public RotationMatrix transpose(RotationMatrix result) {
        return (RotationMatrix) super.transpose(result);
    }

    @Override
    public RotationMatrix add(Matrix3d mat) {
        return (RotationMatrix) super.add(mat);
    }

    public RotationMatrix add(Matrix3d mat, RotationMatrix result) {
        return (RotationMatrix) super.add(mat, result);
    }

    @Override
    public RotationMatrix subtract(Matrix3d mat) {
        return (RotationMatrix) super.subtract(mat);
    }

    public RotationMatrix subtract(Matrix3d mat, RotationMatrix result) {
        return (RotationMatrix) super.subtract(mat, result);
    }

    public RotationMatrix multiply(Matrix3d mat, RotationMatrix result) {
        return (RotationMatrix) super.multiply(mat, result);
    }
}
