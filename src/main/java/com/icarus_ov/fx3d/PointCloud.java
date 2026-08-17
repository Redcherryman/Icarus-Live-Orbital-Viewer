/*
 * Copyright 2026 Aiden Joshua-Steven Scoggins (Aiden J.S. Scoggins)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.icarus_ov.fx3d;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Single-tri-mesh point renderer. Every tracked object is drawn as a tiny
 * filled triangle billboarded along its radius vector. Rendering a whole
 * category as <em>one</em> mesh keeps the view fast for thousands of objects.
 *
 * @author Aiden J.S. Scoggins
 */
public final class PointCloud {

    private static final double SQRT3 = 0.8660254037844386d;

    private final TriangleMesh mesh;
    private final MeshView view;
    private final int capacity;
    private final float[] buffer;

    private PointCloud(final int capacity, final TriangleMesh mesh,
                       final MeshView view, final float[] buffer) {
        this.capacity = capacity;
        this.mesh = mesh;
        this.view = view;
        this.buffer = buffer;
    }

    /**
     * Creates a point cloud with a fixed capacity.
     *
     * @param color    neon marker colour for this category
     * @param capacity maximum number of points it may hold
     * @return the ready-to-add cloud
     */
    public static PointCloud create(final Color color, final int capacity) {
        final int cap = Math.max(1, capacity);
        final TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f);
        final int[] faces = new int[cap * 6];
        for (int i = 0; i < cap; i++) {
            final int b = i * 3;
            final int f = i * 6;
            faces[f] = b; faces[f + 1] = 0;
            faces[f + 2] = b + 1; faces[f + 3] = 1;
            faces[f + 4] = b + 2; faces[f + 5] = 2;
        }
        mesh.getFaces().setAll(faces);
        final MeshView view = new MeshView(mesh);
        view.setDrawMode(DrawMode.FILL);
        view.setCullFace(CullFace.NONE);
        view.setMaterial(new PhongMaterial(color));
        return new PointCloud(cap, mesh, view, new float[cap * 9]);
    }
/**
     * Replaces the first {@code n} points with the given centres.
     *
     * @param centers3n flat xyz array of length {@code 3 * n}
     * @param n         number of points to render
     */
    public void update(final float[] centers3n, final int n) {
        final int count = Math.min(n, capacity);
        for (int i = 0; i < count; i++) {
            final float cx = centers3n[i * 3];
            final float cy = centers3n[i * 3 + 1];
            final float cz = centers3n[i * 3 + 2];
            final double r = Math.sqrt(cx * (double) cx + cy * (double) cy
                    + cz * (double) cz);
            final double nx = cx / r;
            final double ny = cy / r;
            final double nz = cz / r;
            double[] v1;
            if (Math.abs(nz) > 0.999) {
                v1 = cross(nx, ny, nz, 1, 0, 0);
            } else {
                v1 = cross(nx, ny, nz, 0, 0, 1);
            }
            final double[] v2 = cross(nx, ny, nz, v1[0], v1[1], v1[2]);
            final int b = i * 9;
            final double h = 0.008;
            buffer[b] = (float) (cx + h * v1[0]);
            buffer[b + 1] = (float) (cy + h * v1[1]);
            buffer[b + 2] = (float) (cz + h * v1[2]);
            buffer[b + 3] = (float) (cx + h * (-0.5 * v1[0] + SQRT3 * v2[0]));
            buffer[b + 4] = (float) (cy + h * (-0.5 * v1[1] + SQRT3 * v2[1]));
            buffer[b + 5] = (float) (cz + h * (-0.5 * v1[2] + SQRT3 * v2[2]));
            buffer[b + 6] = (float) (cx + h * (-0.5 * v1[0] - SQRT3 * v2[0]));
            buffer[b + 7] = (float) (cy + h * (-0.5 * v1[1] - SQRT3 * v2[1]));
            buffer[b + 8] = (float) (cz + h * (-0.5 * v1[2] - SQRT3 * v2[2]));
        }
        mesh.getPoints().setAll(buffer);
    }

    private static double[] cross(final double ax, final double ay,
                                  final double az, final double bx,
                                  final double by, final double bz) {
        final double x = ay * bz - az * by;
        final double y = az * bx - ax * bz;
        final double z = ax * by - ay * bx;
        final double n = Math.sqrt(x * x + y * y + z * z);
        final double k = n < 1e-12 ? 1.0 : 1.0 / n;
        return new double[]{x * k, y * k, z * k};
    }

    /** The mesh node to add to the scene graph. */
    public MeshView view() {
        return view;
    }

    /** Maximum number of points this cloud can hold. */
    public int capacity() {
        return capacity;
    }
}