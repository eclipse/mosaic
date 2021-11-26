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

import org.eclipse.mosaic.lib.math.Matrix4d;
import org.eclipse.mosaic.lib.math.MatrixAlignment;
import org.eclipse.mosaic.lib.math.Vector3d;

/**
 * An special version of the {@link Matrix4d} providing further methods to
 * describe scaling, rotation, and translation in the 3-dimensional space.
 */
public class TransformationMatrix extends Matrix4d {

    private static final long serialVersionUID = 1L;

    private static final RotationMatrix tmpRot = new RotationMatrix();

    /**
     * Creates a new transformation matrix. The initial values match the identity matrix.
     */
    public TransformationMatrix() {
        setIdentity();
    }

    /**
     * Creates a new transformation matrix with values from the given TransformationMatrix.
     */
    public TransformationMatrix(TransformationMatrix copyFrom) {
        set(copyFrom);
    }

    /**
     * Creates a new transformation matrix. The initial values match the identity matrix.
     */
    public static TransformationMatrix identityMatrix() {
        return new TransformationMatrix();
    }

    /**
     * Extracts a 3x3 sub-matrix containing rotation+scale from this transformation matrix.
     */
    public RotationMatrix getRotation() {
        return getRotation(new RotationMatrix());
    }

    /**
     * Extracts a 3x3 sub-matrix containing rotation+scale from this transformation matrix. The
     * result is written into the result matrix.
     */
    public RotationMatrix getRotation(RotationMatrix result) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                result.set(row, col, get(row, col));
            }
        }
        return result;
    }

    /**
     * Replaces the 3x3 sub-matrix containing the rotation+scale for this transformation matrix.
     */
    public TransformationMatrix setRotation(RotationMatrix rotation) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                set(row, col, rotation.get(row, col));
            }
        }
        return this;
    }

    /**
     * Extracts a 3 dimensional vector containing translation part from this transformation matrix.
     */
    public Vector3d getTranslation() {
        return getTranslation(new Vector3d());
    }

    /**
     * Extracts a 3 dimensional vector containing translation part from this transformation matrix. The
     * result is written into the result vector.
     */
    public Vector3d getTranslation(Vector3d result) {
        result.x = get(0,3);
        result.y = get(1,3);
        result.z = get(2,3);
        return result;
    }

    /**
     * Applies this transformation matrix to the given vector.
     */
    public Vector3d transform(Vector3d vec) {
        return transform(vec, 1d);
    }

    /**
     * Applies this transformation matrix to the given vector.
     */
    public Vector3d transform(Vector3d vec3, double w) {
        double x = vec3.x * m[0] + vec3.y * m[4] + vec3.z * m[8] + w * m[12];
        double y = vec3.x * m[1] + vec3.y * m[5] + vec3.z * m[9] + w * m[13];
        double z = vec3.x * m[2] + vec3.y * m[6] + vec3.z * m[10] + w * m[14];
        return vec3.set(x, y, z);
    }

    /**
     * Adds translation to this transformation matrix.
     */
    public TransformationMatrix translate(Vector3d t) {
        return translate(t.x, t.y, t.z);
    }

    /**
     * Adds translation to this transformation matrix.
     */
    public TransformationMatrix translate(double x, double y, double z) {
        for (int i = 0; i < 4; i++) {
            m[12 + i] += m[i] * x + m[4 + i] * y + m[8 + i] * z;
        }
        return this;
    }

    /**
     * Adds scaling to this transformation matrix.
     */
    public TransformationMatrix scale(double sx, double sy, double sz) {
        for (int i = 0; i < 4; i++) {
            m[i] *= sx;
            m[4 + i] *= sy;
            m[8 + i] *= sz;
        }
        return this;
    }

    /**
     * Adds rotation to this transformation matrix.
     */
    public TransformationMatrix rotate(double angleDeg, Vector3d axis) {
        return rotate(angleDeg, axis.x, axis.y, axis.z);
    }

    /**
     * Adds rotation to this transformation matrix.
     */
    public TransformationMatrix rotate(double angleDeg, double axX, double axY, double axZ) {
        synchronized (tmpRot) {
            setRotation(getRotation(tmpRot).rotate(angleDeg, axX, axY, axZ));
        }
        return this;
    }


    @Override
    public TransformationMatrix copy() {
        return new TransformationMatrix(this);
    }

    @Override
    public TransformationMatrix set(Matrix4d mat) {
        return (TransformationMatrix) super.set(mat);
    }

    @Override
    public TransformationMatrix setIdentity() {
        return (TransformationMatrix) super.setIdentity();
    }

    @Override
    public TransformationMatrix setZero() {
        return (TransformationMatrix) super.setZero();
    }

    @Override
    public TransformationMatrix set(float[] values, MatrixAlignment alignment) {
        return (TransformationMatrix) super.set(values, alignment);
    }

    @Override
    public TransformationMatrix set(double[] values, MatrixAlignment alignment) {
        return (TransformationMatrix) super.set(values, alignment);
    }

    @Override
    public TransformationMatrix set(int row, int col, double value) {
        return (TransformationMatrix) super.set(row, col, value);
    }

    @Override
    public TransformationMatrix transpose() {
        return (TransformationMatrix) super.transpose();
    }

    public TransformationMatrix transpose(TransformationMatrix result) {
        return (TransformationMatrix) super.transpose(result);
    }

    @Override
    public TransformationMatrix add(Matrix4d mat) {
        return (TransformationMatrix) super.add(mat);
    }

    public TransformationMatrix add(Matrix4d mat, TransformationMatrix result) {
        return (TransformationMatrix) super.add(mat, result);
    }

    @Override
    public TransformationMatrix subtract(Matrix4d mat) {
        return (TransformationMatrix) super.subtract(mat);
    }

    public TransformationMatrix subtract(Matrix4d mat, TransformationMatrix result) {
        return (TransformationMatrix) super.subtract(mat, result);
    }

    public TransformationMatrix multiply(Matrix4d mat, TransformationMatrix result) {
        return (TransformationMatrix) super.multiply(mat, result);
    }

}
