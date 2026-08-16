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
package com.icarus_ov.data;

import com.icarus_ov.model.SpaceObject;
import com.icarus_ov.model.SpaceObjectType;

import java.util.List;
import java.util.Map;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * An offline fallback catalog. Guarantees the app always has something
 * meaningful to render even when no network is available, and demonstrates
 * the full range of object categories.
 *
 * @author Aiden J.S. Scoggins
 */
public final class SampleCatalog {

    private SampleCatalog() {
    }

    /** Returns a small representative catalog covering every category. */
    public static List<SpaceObject> load() {
        return List.of(
            new SpaceObject("ISS (ZARYA)", "25544", SpaceObjectType.ISS,
                    "sample", null, null, Map.of("note", "canned offline sample")),
            new SpaceObject("HUBBLE SPACE TELESCOPE", "20580", SpaceObjectType.SATELLITE,
                    "sample", null, null, Map.of()),
            new SpaceObject("STARLINK-30243", "47623", SpaceObjectType.STARLINK,
                    "sample", null, null, Map.of()),
            new SpaceObject("SL-6 R/B(2) (DEB)", "19653", SpaceObjectType.DEBRIS,
                    "sample", null, null, Map.of("piece", "uncontrolled rocket body")),
            new SpaceObject("MOLNIYA 1-44 DEB", "23600", SpaceObjectType.DEBRIS,
                    "sample", null, null, Map.of("piece", "fragmentation debris")),
            new SpaceObject("ASTEROID-2023-BU2", "SAMPLE-A1", SpaceObjectType.ASTEROID,
                    "sample", null, null, Map.of("note", "canned near-Earth object")),
            new SpaceObject("FIREBALL-2026-0001", "SAMPLE-FB1", SpaceObjectType.FIREBALL,
                    "sample", null, null, Map.of("note", "canned bolide event"))
        );
    }
}