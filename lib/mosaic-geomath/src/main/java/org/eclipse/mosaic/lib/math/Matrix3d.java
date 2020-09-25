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

package org.eclipse.mosaic.lib.math;

public class Matrix3d {

    public final double m[] = new double[9];

    public static Matrix3d identityMatrix() {
        return new Matrix3d().loadIdentity();
    }

    public static Matrix3d copy(Matrix3d m) {
        return new Matrix3d().set(m.m);
    }

    public Matrix3d loadIdentity() {
        m[0] = 1;
        m[1] = 0;
        m[2] = 0;

        m[3] = 0;
        m[4] = 1;
        m[5] = 0;

        m[6] = 0;
        m[7] = 0;
        m[8] = 1;
        return this;
    }

    public Matrix3d set(Matrix3d m) {
        set(m.m);
        return this;
    }

    public Matrix3d set(double[] data) {
        System.arraycopy(data, 0, m, 0, 9);
        return this;
    }

    public Matrix3d transpose() {
        double d;
        d = m[1];
        m[1] = m[3];
        m[3] = d;
        d = m[2];
        m[2] = m[6];
        m[6] = d;
        d = m[5];
        m[5] = m[7];
        m[7] = d;
        return this;
    }

    public Matrix3d transpose(Matrix3d result) {
        result.m[0] = m[0];
        result.m[1] = m[3];
        result.m[2] = m[6];

        result.m[3] = m[1];
        result.m[4] = m[4];
        result.m[5] = m[7];

        result.m[6] = m[2];
        result.m[7] = m[5];
        result.m[8] = m[8];
        return result;
    }

    public Matrix3d multiply(Matrix3d mat, Matrix3d result) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double x = 0;
                for (int k = 0; k < 3; k++) {
                    x += m[j + k * 3] * mat.m[i * 3 + k];
                }
                result.m[i * 3 + j] = x;
            }
        }
        return result;
    }

    public Vector3d multiply(Vector3d v) {
        double x = v.x * m[0] + v.y * m[1] + v.z * m[2];
        double y = v.x * m[3] + v.y * m[4] + v.z * m[5];
        double z = v.x * m[6] + v.y * m[7] + v.z * m[8];
        v.set(x, y, z);
        return v;
    }

    public Vector3d multiply(Vector3d v, Vector3d result) {
        result.x = v.x * m[0] + v.y * m[1] + v.z * m[2];
        result.y = v.x * m[3] + v.y * m[4] + v.z * m[5];
        result.z = v.x * m[6] + v.y * m[7] + v.z * m[8];
        return result;
    }

//    public void orthonormalize() {
//        Vector3d x = new Vector3d(m[0], m[3], m[6]);
//        Vector3d y = new Vector3d(m[1], m[4], m[7]);
//        Vector3d z = new Vector3d();
//
//        x.norm();
//        x.cross(y, z);
//        z.norm();
//        z.cross(x, y);
//        y.norm();
//
//        m[0] = x.x;
//        m[3] = x.y;
//        m[6] = x.z;
//        m[1] = y.x;
//        m[4] = y.y;
//        m[7] = y.z;
//        m[2] = z.x;
//        m[5] = z.y;
//        m[8] = z.z;
//    }
}