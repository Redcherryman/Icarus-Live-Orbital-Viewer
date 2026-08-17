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

import com.icarus_ov.model.SpaceObject;
import com.icarus_ov.model.SpaceObjectType;
import com.icarus_ov.model.TrackPoint;
import com.icarus_ov.propagation.PropagationEngine;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * The full 3D scene: neon-wireframe Earth, starfield, one point cloud per
 * object category, an orbit camera rig, and a follow-ISS control. On a timer
 * it re-propagates the visible catalog to "now" and repaints the clouds.
 *
 * @author Aiden J.S. Scoggins
 */
public final class LiveViewport {

    private static final int CLOUD_CAPACITY = 12000;
    private static final int STAR_COUNT = 1400;
    private static final double TICK_MS = 500.0;

    private final PropagationEngine engine;
    private final ObservableList<SpaceObject> shown;
    private final CameraRig rig = new CameraRig();

    private final Map<SpaceObjectType, PointCloud> clouds =
            new EnumMap<>(SpaceObjectType.class);
    private final Map<SpaceObjectType, float[]> buffers =
            new EnumMap<>(SpaceObjectType.class);
    private final Map<SpaceObjectType, Integer> counts =
            new EnumMap<>(SpaceObjectType.class);

    private final double[] issScene = new double[3];
    private final Timeline loop;

    /** Builds the viewport bound to the given engine and visible list. */
    public LiveViewport(final PropagationEngine engine,
                        final ObservableList<SpaceObject> shown) {
        this.engine = engine;
        this.shown = shown;
        for (final SpaceObjectType type : SpaceObjectType.values()) {
            clouds.put(type, PointCloud.create(type.markerColor(), CLOUD_CAPACITY));
            buffers.put(type, new float[CLOUD_CAPACITY * 3]);
            counts.put(type, 0);
        }
        this.loop = new Timeline(new KeyFrame(Duration.millis(TICK_MS),
                e -> tick()));
        this.loop.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Builds the scene and drops it into the provided holder pane.
     *
     * @param holder the retro "viewport" StackPane from the main UI
     */
    public void attach(final StackPane holder) {
        final Group world = new Group();
        world.getChildren().add(RetroEarth.build());
        world.getChildren().add(buildStars().view());
        for (final PointCloud cloud : clouds.values()) {
            world.getChildren().add(cloud.view());
        }
        world.getChildren().add(rig.node());

        final SubScene sub = new SubScene(world, 100, 100,
                true, SceneAntialiasing.BALANCED);
        sub.widthProperty().bind(holder.widthProperty());
        sub.heightProperty().bind(holder.heightProperty());
        sub.setFill(Color.web("#02040a"));
        rig.install(sub);

        final Button follow = new Button("FOLLOW ISS");
        follow.getStyleClass().add("hud-button");
        follow.setOnAction(e -> rig.setFollow(!rig.isFollow(),
                issScene[0], issScene[1], issScene[2]));

        holder.getChildren().add(sub);
        holder.getChildren().add(follow);
        StackPane.setAlignment(follow, Pos.BOTTOM_CENTER);
        StackPane.setMargin(follow, new Insets(0, 0, 10, 0));
    }

    /** Starts the live update loop. */
    public void start() {
        loop.play();
    }

    /** Stops the live update loop. */
    public void stop() {
        loop.stop();
    }
/** A static faint starfield around the scene. */
    private static PointCloud buildStars() {
        final PointCloud stars = PointCloud.create(
                Color.web("#cfeaff", 0.55), STAR_COUNT);
        final Random rnd = new Random(42L);
        final float[] pos = new float[STAR_COUNT * 3];
        for (int i = 0; i < STAR_COUNT; i++) {
            final double z = 2.0 * rnd.nextDouble() - 1.0;
            final double a = 2.0 * Math.PI * rnd.nextDouble();
            final double r = Math.sqrt(Math.max(0.0, 1.0 - z * z));
            final double radius = 80.0 + 40.0 * rnd.nextDouble();
            pos[i * 3] = (float) (radius * r * Math.cos(a));
            pos[i * 3 + 1] = (float) (radius * z);
            pos[i * 3 + 2] = (float) (radius * r * Math.sin(a));
        }
        stars.update(pos, STAR_COUNT);
        return stars;
    }

    /** Re-propagates the visible catalog and repaints the point clouds. */
    private void tick() {
        final var date = engine.now();
        counts.replaceAll((k, v) -> 0);
        for (final SpaceObject s : shown) {
            final TrackPoint tp = engine.propagate(s, date);
            if (tp == null) {
                continue;
            }
            final double[] sc = CoordinateMath.toScene(tp);
            if (s.type() == SpaceObjectType.ISS) {
                issScene[0] = sc[0];
                issScene[1] = sc[1];
                issScene[2] = sc[2];
            }
            final int idx = counts.merge(s.type(), 1, Integer::sum) - 1;
            final float[] buf = buffers.get(s.type());
            final int base = idx * 3;
            if (base + 3 <= buf.length) {
                buf[base] = (float) sc[0];
                buf[base + 1] = (float) sc[1];
                buf[base + 2] = (float) sc[2];
            }
        }
        for (final Map.Entry<SpaceObjectType, PointCloud> e : clouds.entrySet()) {
            final int n = counts.getOrDefault(e.getKey(), 0);
            if (n > 0) {
                e.getValue().update(buffers.get(e.getKey()), n);
            }
        }
        rig.updateFollow();
    }
}