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

import com.icarus_ov.model.TrackPoint;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Coordinate math between ECI metres (from SGP4) and the scene's unit space,
 * where the Earth's mean radius equals {@code 1.0}.
 *
 * @author Aiden J.S. Scoggins
 */
public final class CoordinateMath {

    /** Earth mean radius, metres. Scene unit = one Earth radius. */
    public static final double EARTH_RADIUS_M = 6371008.8;

    private CoordinateMath() {
    }

    /**
     * Converts a propagated track point into scene coordinates.
     *
     * @param tp the propagated snapshot
     * @return a 3-element scene position {x, y, z} (Earth radius = 1)
     */
    public static double[] toScene(final TrackPoint tp) {
        final double k = 1.0 / EARTH_RADIUS_M;
        return new double[]{tp.eciXM * k, tp.eciYM * k, tp.eciZM * k};
    }
}