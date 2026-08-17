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
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Orbit camera rig: mouse-drag to yaw/pitch, scroll wheel to zoom, and an
 * optional "follow" mode that points the camera at a target scene position.
 *
 * @author Aiden J.S. Scoggins
 */
public final class CameraRig {

    private final PerspectiveCamera camera;
    private final Group yawPivot = new Group();
    private final Group pitchPivot = new Group();
    private final Rotate yaw;
    private final Rotate pitch;
    private double distance = 7.0;
    private boolean follow;
    private final double[] followTarget = new double[3];

    /** Builds the rig with a near/far clip appropriate for the scene. */
    public CameraRig() {
        this.camera = new PerspectiveCamera(true);
        camera.setNearClip(0.01);
        camera.setFarClip(2000);
        camera.setFieldOfView(55);
        camera.setTranslateZ(-distance);

        yaw = new Rotate(30, 0, 0, 0, Rotate.Y_AXIS);
        pitch = new Rotate(-15, 0, 0, 0, Rotate.X_AXIS);
        pitchPivot.getTransforms().add(pitch);
        yawPivot.getTransforms().add(yaw);
        pitchPivot.getChildren().add(camera);
        yawPivot.getChildren().add(pitchPivot);
    }

    /**
     * Attaches drag and scroll handlers to the given scene.
     *
     * @param scene the 3D sub-scene that receives pointer input
     */
    public void install(final SubScene scene) {
        final double[] drag = new double[2];
        scene.setOnMousePressed(e -> {
            drag[0] = e.getX();
            drag[1] = e.getY();
        });
        scene.setOnMouseDragged(e -> {
            if (follow) {
                return;
            }
            final double dx = e.getX() - drag[0];
            final double dy = e.getY() - drag[1];
            drag[0] = e.getX();
            drag[1] = e.getY();
            yaw.setAngle(norm180(yaw.getAngle() - dx * 0.45));
            pitch.setAngle(clamp(pitch.getAngle() - dy * 0.45, -88, 88));
        });
        scene.addEventHandler(ScrollEvent.SCROLL, e -> {
            if (follow) {
                return;
            }
            distance = clamp(distance * (1.0 + e.getDeltaY() * 0.002), 2.5, 90);
        });
        scene.setCamera(camera);
    }

    /** The node that must be added to the 3D world group. */
    public Group node() {
        return yawPivot;
    }

    /** The camera used by the sub-scene. */
    public PerspectiveCamera camera() {
        return camera;
    }

    /** Toggles follow mode (camera chases {@code target} until disabled). */
    public void setFollow(final boolean value, final double x,
                          final double y, final double z) {
        this.follow = value;
        if (value) {
            this.followTarget[0] = x;
            this.followTarget[1] = y;
            this.followTarget[2] = z;
        }
    }

    public boolean isFollow() {
        return follow;
    }

    /** Repoints the camera at the follow target if follow mode is active. */
    public void updateFollow() {
        if (!follow) {
            return;
        }
        final double px = followTarget[0];
        final double py = followTarget[1];
        final double pz = followTarget[2];
        final double r = Math.sqrt(px * px + py * py + pz * pz);
        final double a = clamp(Math.toDegrees(Math.asin(py / r)), -88, 88);
        final double b = Math.toDegrees(Math.atan2(-px, -pz));
        pitch.setAngle(a);
        yaw.setAngle(norm180(b));
    }

    private static double clamp(final double v, final double lo,
                                final double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double norm180(final double deg) {
        return ((deg + 540.0) % 360.0) - 180.0;
    }
}