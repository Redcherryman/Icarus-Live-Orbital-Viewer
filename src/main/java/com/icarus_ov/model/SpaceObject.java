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

import java.util.Map;
import java.util.Objects;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * An immutable description of one tracked object (satellite, debris, asteroid,
 * or fireball event) as reported by a public NASA/CelesTrak source.
 *
 * @author Aiden J.S. Scoggins
 */
public final class SpaceObject {

    private final String name;
    private final String id;
    private final SpaceObjectType type;
    private final String source;
    private final String tleLine1;
    private final String tleLine2;
    private final Map<String, String> properties;

    /**
     * Full constructor.
     *
     * @param name       display name (e.g. "ISS (ZARYA)")
     * @param id         catalog / record identifier (e.g. NORAD 25544)
     * @param type       category of the object
     * @param source     public source that supplied the record (e.g. "CelesTrak")
     * @param tleLine1   first TLE line ({@code null} if not applicable)
     * @param tleLine2   second TLE line ({@code null} if not applicable)
     * @param properties extra ad-hoc telemetry keys exposed to the HUD
     */
    public SpaceObject(final String name,
                       final String id,
                       final SpaceObjectType type,
                       final String source,
                       final String tleLine1,
                       final String tleLine2,
                       final Map<String, String> properties) {
        this.name = Objects.requireNonNull(name, "name");
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.source = Objects.requireNonNull(source, "source");
        this.tleLine1 = tleLine1;
        this.tleLine2 = tleLine2;
        this.properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    /** Display name of the object. */
    public String name() {
        return name;
    }

    /** Catalog / record identifier. */
    public String id() {
        return id;
    }

    /** Category of the object. */
    public SpaceObjectType type() {
        return type;
    }

    /** Public source that supplied this record. */
    public String source() {
        return source;
    }

    /** First TLE line, or {@code null} when not applicable. */
    public String tleLine1() {
        return tleLine1;
    }

    /** Second TLE line, or {@code null} when not applicable. */
    public String tleLine2() {
        return tleLine2;
    }

    /** Immutable bag of extra telemetry (altitude, velocity, date, ...). */
    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, type, source);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SpaceObject)) {
            return false;
        }
        final SpaceObject other = (SpaceObject) obj;
        return Objects.equals(name, other.name)
            && Objects.equals(id, other.id)
            && type == other.type
            && Objects.equals(source, other.source);
    }

    @Override
    public String toString() {
        return String.format("%-18s [%s :: %s]",
                name, type.displayName(), id);
    }
}