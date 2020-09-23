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

package org.eclipse.mosaic.lib.math;

public class Matrix4d {

    private static final Matrix4d tmpMatA = new Matrix4d();
    private static final Matrix4d tmpMatB = new Matrix4d();

    public final double[] m = new double[16];

    public Matrix4d() {
        setZero();
    }

    public void getGlTransform(float[] glTransform) {
        for (int i = 0; i < 16; i++) {
            glTransform[i] = (float) m[i];
        }
    }

    public void dump() {
        for (int m = 0; m < 4; m++) {
            for (int n = 0; n < 4; n++) {
                System.out.printf("%8.3f ", this.m[n * 4 + m]);
            }
            System.out.println();
        }
    }

    public Matrix4d translate(Vector3d t) {
        return translate(t.x, t.y, t.z);
    }

    public Matrix4d translate(double x, double y, double z) {
        for (int i = 0; i < 4; i++) {
            m[12 + i] += m[i] * x + m[4 + i] * y + m[8 + i] * z;
        }
        return this;
    }

    public Matrix4d rotate(double angleDeg, Vector3d axis) {
        return rotate(angleDeg, axis.x, axis.y, axis.z);
    }

    public Matrix4d scale(double sx, double sy, double sz) {
        for (int i = 0; i < 4; i++) {
            m[i] *= sx;
            m[4 + i] *= sy;
            m[8 + i] *= sz;
        }
        return this;
    }

    public Matrix4d rotate(double angleDeg, double axX, double axY, double axZ) {
        synchronized (tmpMatA) {
            tmpMatA.setRotate(angleDeg, axX, axY, axZ);
            set(multiply(tmpMatA, tmpMatB));
        }
        return this;
    }

    public Matrix4d setIdentity() {
        for (int i = 0; i < 16; i++) {
            m[i] = i % 5 == 0 ? 1 : 0;
        }
        return this;
    }

    public Matrix4d setZero() {
        for (int i = 0; i < 16; i++) {
            m[i] = 0;
        }
        return this;
    }

    public Matrix4d set(Matrix4d mat) {
        for (int i = 0; i < 16; i++) {
            m[i] = mat.m[i];
        }
        return this;
    }

    public Matrix4d add(Matrix4d mat) {
        for (int i = 0; i < 16; i++) {
            m[i] += mat.m[i];
        }
        return this;
    }

    public Matrix4d multiply(Matrix4d mat, Matrix4d result) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double x = 0;
                for (int k = 0; k < 4; k++) {
                    x += m[j + k * 4] * mat.m[i * 4 + k];
                }
                result.m[i * 4 + j] = x;
            }
        }
        return result;
    }

    public Vector3d transform(Vector3d vec3, double w) {
        double x = vec3.x * m[0] + vec3.y * m[4] + vec3.z * m[8] + w * m[12];
        double y = vec3.x * m[1] + vec3.y * m[5] + vec3.z * m[9] + w * m[13];
        double z = vec3.x * m[2] + vec3.y * m[6] + vec3.z * m[10] + w * m[14];
        return vec3.set(x, y, z);
    }

    public Matrix4d transpose() {
        for (int m = 0; m < 4; m++) {
            for (int n = m + 1; n < 4; n++) {
                swap(m * 4 + n, n * 4 + m);
            }
        }
        return this;
    }

    private void swap(int i, int j) {
        double d = m[i];
        m[i] = m[j];
        m[j] = d;
    }

    public double get(int row, int col) {
        return m[row * 4 + col];
    }

    public void set(int row, int col, double value) {
        m[row * 4 + col] = value;
    }

    public Matrix4d setRotate(double angleDeg, double axX, double axY, double axZ) {
        double a = Math.toRadians(angleDeg);
        double x = axX;
        double y = axY;
        double z = axZ;
        m[3] = 0.0;
        m[7] = 0.0;
        m[11] = 0.0;
        m[12] = 0.0;
        m[13] = 0.0;
        m[14] = 0.0;
        m[15] = 1.0;
        double s = Math.sin(a);
        double c = Math.cos(a);
        if (1.0f == x && 0.0f == y && 0.0f == z) {
            m[5] = c;
            m[10] = c;
            m[6] = s;
            m[9] = -s;
            m[1] = 0f;
            m[2] = 0f;
            m[4] = 0f;
            m[8] = 0f;
            m[0] = 1f;
        } else if (0.0f == x && 1.0f == y && 0.0f == z) {
            m[0] = c;
            m[10] = c;
            m[8] = s;
            m[2] = -s;
            m[1] = 0f;
            m[4] = 0f;
            m[6] = 0f;
            m[9] = 0f;
            m[5] = 1f;
        } else if (0.0f == x && 0.0f == y && 1.0f == z) {
            m[0] = c;
            m[5] = c;
            m[1] = s;
            m[4] = -s;
            m[2] = 0f;
            m[6] = 0f;
            m[8] = 0f;
            m[9] = 0f;
            m[10] = 1f;
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
            m[4] = xy * nc - zs;
            m[8] = zx * nc + ys;
            m[1] = xy * nc + zs;
            m[5] = y * y * nc + c;
            m[9] = yz * nc - xs;
            m[2] = zx * nc - ys;
            m[6] = yz * nc + xs;
            m[10] = z * z * nc + c;
        }
        return this;
    }

    public boolean invert() {
        // Invert a 4 x 4 matrix using Cramer's Rule

        // transpose matrix
        final double src0 = m[0];
        final double src4 = m[1];
        final double src8 = m[2];
        final double src12 = m[3];

        final double src1 = m[4];
        final double src5 = m[5];
        final double src9 = m[6];
        final double src13 = m[7];

        final double src2 = m[8];
        final double src6 = m[9];
        final double src10 = m[10];
        final double src14 = m[11];

        final double src3 = m[12];
        final double src7 = m[13];
        final double src11 = m[14];
        final double src15 = m[15];

        // calculate pairs for first 8 elements (cofactors)
        final double atmp0 = src10 * src15;
        final double atmp1 = src11 * src14;
        final double atmp2 = src9 * src15;
        final double atmp3 = src11 * src13;
        final double atmp4 = src9 * src14;
        final double atmp5 = src10 * src13;
        final double atmp6 = src8 * src15;
        final double atmp7 = src11 * src12;
        final double atmp8 = src8 * src14;
        final double atmp9 = src10 * src12;
        final double atmp10 = src8 * src13;
        final double atmp11 = src9 * src12;

        // calculate first 8 elements (cofactors)
        final double dst0 = (atmp0 * src5 + atmp3 * src6 + atmp4 * src7)
                - (atmp1 * src5 + atmp2 * src6 + atmp5 * src7);
        final double dst1 = (atmp1 * src4 + atmp6 * src6 + atmp9 * src7)
                - (atmp0 * src4 + atmp7 * src6 + atmp8 * src7);
        final double dst2 = (atmp2 * src4 + atmp7 * src5 + atmp10 * src7)
                - (atmp3 * src4 + atmp6 * src5 + atmp11 * src7);
        final double dst3 = (atmp5 * src4 + atmp8 * src5 + atmp11 * src6)
                - (atmp4 * src4 + atmp9 * src5 + atmp10 * src6);
        final double dst4 = (atmp1 * src1 + atmp2 * src2 + atmp5 * src3)
                - (atmp0 * src1 + atmp3 * src2 + atmp4 * src3);
        final double dst5 = (atmp0 * src0 + atmp7 * src2 + atmp8 * src3)
                - (atmp1 * src0 + atmp6 * src2 + atmp9 * src3);
        final double dst6 = (atmp3 * src0 + atmp6 * src1 + atmp11 * src3)
                - (atmp2 * src0 + atmp7 * src1 + atmp10 * src3);
        final double dst7 = (atmp4 * src0 + atmp9 * src1 + atmp10 * src2)
                - (atmp5 * src0 + atmp8 * src1 + atmp11 * src2);

        // calculate pairs for second 8 elements (cofactors)
        final double btmp0 = src2 * src7;
        final double btmp1 = src3 * src6;
        final double btmp2 = src1 * src7;
        final double btmp3 = src3 * src5;
        final double btmp4 = src1 * src6;
        final double btmp5 = src2 * src5;
        final double btmp6 = src0 * src7;
        final double btmp7 = src3 * src4;
        final double btmp8 = src0 * src6;
        final double btmp9 = src2 * src4;
        final double btmp10 = src0 * src5;
        final double btmp11 = src1 * src4;

        // calculate second 8 elements (cofactors)
        final double dst8 = (btmp0 * src13 + btmp3 * src14 + btmp4 * src15)
                - (btmp1 * src13 + btmp2 * src14 + btmp5 * src15);
        final double dst9 = (btmp1 * src12 + btmp6 * src14 + btmp9 * src15)
                - (btmp0 * src12 + btmp7 * src14 + btmp8 * src15);
        final double dst10 = (btmp2 * src12 + btmp7 * src13 + btmp10 * src15)
                - (btmp3 * src12 + btmp6 * src13 + btmp11 * src15);
        final double dst11 = (btmp5 * src12 + btmp8 * src13 + btmp11 * src14)
                - (btmp4 * src12 + btmp9 * src13 + btmp10 * src14);
        final double dst12 = (btmp2 * src10 + btmp5 * src11 + btmp1 * src9)
                - (btmp4 * src11 + btmp0 * src9 + btmp3 * src10);
        final double dst13 = (btmp8 * src11 + btmp0 * src8 + btmp7 * src10)
                - (btmp6 * src10 + btmp9 * src11 + btmp1 * src8);
        final double dst14 = (btmp6 * src9 + btmp11 * src11 + btmp3 * src8)
                - (btmp10 * src11 + btmp2 * src8 + btmp7 * src9);
        final double dst15 = (btmp10 * src10 + btmp4 * src8 + btmp9 * src9)
                - (btmp8 * src9 + btmp11 * src10 + btmp5 * src8);

        // calculate determinant
        final double det = src0 * dst0 + src1 * dst1 + src2 * dst2 + src3 * dst3;
        if (det == 0.0f) {
            return false;
        }

        // calculate matrix inverse
        final double invdet = 1.0f / det;
        m[0] = dst0 * invdet;
        m[1] = dst1 * invdet;
        m[2] = dst2 * invdet;
        m[3] = dst3 * invdet;

        m[4] = dst4 * invdet;
        m[5] = dst5 * invdet;
        m[6] = dst6 * invdet;
        m[7] = dst7 * invdet;

        m[8] = dst8 * invdet;
        m[9] = dst9 * invdet;
        m[10] = dst10 * invdet;
        m[11] = dst11 * invdet;

        m[12] = dst12 * invdet;
        m[13] = dst13 * invdet;
        m[14] = dst14 * invdet;
        m[15] = dst15 * invdet;

        return true;
    }
}