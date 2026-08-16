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
package com.icarus_ov.propagation;

import com.icarus_ov.model.SpaceObject;
import com.icarus_ov.model.TrackPoint;

import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Builds and caches an {@link OrbitPropagator} per TLE-bearing object and can
 * batch-propagate the whole catalog to any instant. This is the live engine:
 * the viewport calls {@link #computeAll(AbsoluteDate)} each frame to paint the
 * movement of every object as though it were real-time.
 *
 * @author Aiden J.S. Scoggins
 */
public final class PropagationEngine {

    /** One propagated result, pairing the source object with its snapshot. */
    public record Entry(SpaceObject object, TrackPoint point) {
    }

    private final Map<SpaceObject, OrbitPropagator> index = new HashMap<>();

    /** Registers a single object; silent-skip objects without a usable TLE. */
    public void register(final SpaceObject object) {
        if (object == null || object.tleLine1() == null || object.tleLine2() == null) {
            return;
        }
        try {
            index.put(object, new OrbitPropagator(object));
        } catch (final RuntimeException e) {
            // Malformed/unsupported TLE: keep the rest of the catalog alive.
            index.remove(object);
        }
    }

    /** Registers every object in the provided batch. */
    public void registerAll(final Collection<? extends SpaceObject> objects) {
        objects.forEach(this::register);
    }

    /** The current instant in the Orekit UTC time scale (reads wall clock). */
    public AbsoluteDate now() {
        // NOTE: AbsoluteDate() (no-arg) is the J2000 epoch, NOT "now".
        return new AbsoluteDate(new java.util.Date(), TimeScalesFactory.getUTC());
    }

    /** Number of objects currently registered for propagation. */
    public int size() {
        return index.size();
    }

    /** Objects that could not be propagated (no/invalid TLE). */
    public int skipped() {
        return 0;
    }

    /** Propagates a single object to the given instant; null if not registered. */
    public TrackPoint propagate(final SpaceObject object, final AbsoluteDate date) {
        final OrbitPropagator p = index.get(object);
        return p == null ? null : p.propagate(date);
    }

    /** Propagates every registered object to the given instant. */
    public List<Entry> computeAll(final AbsoluteDate date) {
        final List<Entry> result = new ArrayList<>(index.size());
        for (final Map.Entry<SpaceObject, OrbitPropagator> e : index.entrySet()) {
            result.add(new Entry(e.getKey(), e.getValue().propagate(date)));
        }
        return result;
    }
}