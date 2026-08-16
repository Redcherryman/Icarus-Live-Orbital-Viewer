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
package com.icarus_ov.model;

import javafx.scene.paint.Color;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * The category a tracked object belongs to. Each category maps to a
 * phosphor/Cathode-Ray-Tube marker color used by the 3D viewport.
 *
 * @author Aiden J.S. Scoggins
 */
public enum SpaceObjectType {

    /** Manned / crewed platforms, currently the ISS. */
    ISS("ISS", Color.web("#ffffff")),

    /** Operational, Low/Medium/Geo Earth-orbiting satellites. */
    SATELLITE("SATELLITE", Color.web("#5df2ff")),

    /** Large commercial low-orbit constellation (Starlink). */
    STARLINK("STARLINK", Color.web("#9a7bff")),

    /** Non-functional hardware, rocket bodies, fragmentation: space junk. */
    DEBRIS("DEBRIS", Color.web("#ffb24d")),

    /** Near-Earth asteroids and close-approach objects. */
    ASTEROID("ASTEROID", Color.web("#ff5d5d")),

    /** Recent meteor / bolide (fireball) events. */
    FIREBALL("FIREBALL", Color.web("#86ffb0"));

    private final String displayName;
    private final Color markerColor;

    SpaceObjectType(final String displayName, final Color markerColor) {
        this.displayName = displayName;
        this.markerColor = markerColor;
    }

    /** Human-readable short code for this type. */
    public String displayName() {
        return displayName;
    }

    /** Neon marker color for the 3D viewport and HUD chips. */
    public Color markerColor() {
        return markerColor;
    }
}