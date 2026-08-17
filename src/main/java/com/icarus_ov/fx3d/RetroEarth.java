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

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Retro-futurist neon-wireframe Earth: a triangulated globe rendered with
 * {@code DrawMode.LINE} plus faint pole markers. Single mesh = cheap to draw.
 *
 * @author Aiden J.S. Scoggins
 */
public final class RetroEarth {

    private RetroEarth() {
    }

    /**
     * Builds the wireframe globe group (radius 1.0 scene unit).
     *
     * @return the earth group
     */
    public static Node build() {
        final MeshView globe = new MeshView(sphereMesh(1.0, 28, 18));
        globe.setDrawMode(DrawMode.LINE);
        globe.setCullFace(CullFace.BACK);
        final PhongMaterial wire = new PhongMaterial(Color.web("#39c5e0"));
        globe.setMaterial(wire);

        final Sphere north = pole(Color.web("#5df2ff"));
        north.setTranslateZ(1.0);
        final Sphere south = pole(Color.web("#ffb24d"));
        south.setTranslateZ(-1.0);

        return new Group(globe, north, south);
    }

    private static Sphere pole(final Color color) {
        final Sphere p = new Sphere(0.02);
        p.setMaterial(new PhongMaterial(color));
        return p;
    }

    /**
     * Builds a UV-sphere triangle mesh.
     *
     * @param radius globe radius
     * @param slices longitude subdivisions
     * @param stacks latitude subdivisions
     * @return the triangle mesh
     */
    public static TriangleMesh sphereMesh(final double radius,
                                          final int slices,
                                          final int stacks) {
        final float[] points = new float[(slices + 1) * (stacks + 1) * 3];
        final float[] tex = new float[(slices + 1) * (stacks + 1) * 2];
        int pi = 0;
        int ti = 0;
        for (int s = 0; s <= stacks; s++) {
            final double phi = Math.PI * s / stacks;           // 0..PI
            for (int l = 0; l <= slices; l++) {
                final double theta = 2.0 * Math.PI * l / slices; // 0..2PI
                final double y = Math.cos(phi);
                final double x = Math.sin(phi) * Math.cos(theta);
                final double z = Math.sin(phi) * Math.sin(theta);
                points[pi++] = (float) (radius * x);
                points[pi++] = (float) (radius * y);
                points[pi++] = (float) (radius * z);
                tex[ti++] = (float) (1.0 * l / slices);
                tex[ti++] = (float) (1.0 * s / stacks);
            }
        }
        final int[] faces = new int[slices * stacks * 12];
        int fi = 0;
        for (int s = 0; s < stacks; s++) {
            for (int l = 0; l < slices; l++) {
                final int a = s * (slices + 1) + l;
                final int b = a + 1;
                final int c = (s + 1) * (slices + 1) + l;
                final int d = c + 1;
                faces[fi++] = a; faces[fi++] = 0;
                faces[fi++] = c; faces[fi++] = 2;
                faces[fi++] = b; faces[fi++] = 1;
                faces[fi++] = b; faces[fi++] = 1;
                faces[fi++] = c; faces[fi++] = 2;
                faces[fi++] = d; faces[fi++] = 3;
            }
        }
        final TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(points);
        mesh.getTexCoords().setAll(tex);
        mesh.getFaces().setAll(faces);
        return mesh;
    }
}