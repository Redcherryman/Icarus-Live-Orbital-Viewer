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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Aggregates every free public source into one catalog. Live feeds are always
 * attempted first; the bundled {@link SampleCatalog} backs the app up so the
 * viewport is never empty.
 *
 * @author Aiden J.S. Scoggins
 */
public final class DataService {

    private final CelesTrakClient celesTrak = new CelesTrakClient();
    private final JplApiClient jpl = new JplApiClient();

    /** Event feed callback receives status / error strings for the HUD. */
    public interface Log {
        void log(String line);
    }

    /**
     * Loads the live catalog from all public sources.
     *
     * @param log callback used to surface per-source status to the UI
     * @return merged, de-duplicated list of tracked objects
     */
    public List<SpaceObject> loadAll(final Log log) {
        final Map<String, SpaceObject> byKey = new LinkedHashMap<>();

        // Offline base so the app is never empty.
        for (final SpaceObject s : SampleCatalog.load()) {
            byKey.put(key(s), s);
        }

        log.log(">> contact CelesTrak / stations (ISS + manned) ...");
        addAll(byKey, celesTrak.fetchGroup("stations", SpaceObjectType.SATELLITE, 0),
                log, "CelesTrak stations");
        politeDelay();

        log.log(">> contact CelesTrak / all active satellites ...");
        addAll(byKey, celesTrak.fetchGroup("active", SpaceObjectType.SATELLITE, 5000),
                log, "CelesTrak active");
        politeDelay();

        log.log(">> contact CelesTrak / bright visual satellites ...");
        addAll(byKey, celesTrak.fetchGroup("visual", SpaceObjectType.SATELLITE, 400),
                log, "CelesTrak visual");
        politeDelay();

        log.log(">> contact CelesTrak / starlink constellation ...");
        addAll(byKey, celesTrak.fetchGroup("starlink", SpaceObjectType.STARLINK, 2000),
                log, "CelesTrak starlink");
        politeDelay();

        // NOTE: the truly "ALL tracked debris" catalog (fragmentation, derelict
        // rocket bodies, space junk) is only published on Space-Track.org, which
        // requires an account. ICARUS-OV is a public-only release and therefore
        // deliberately omits it. The sample catalog supplies a few debris entries.
        log.log("   [skipped] full debris catalog requires Space-Track login");

        log.log(">> contact NASA JPL SSD / asteroids ...");
        for (final SpaceObject a : jpl.fetchCloseApproaches(20)) {
            byKey.putIfAbsent(key(a), a);
        }

        log.log(">> contact NASA JPL SSD / fireballs ...");
        for (final SpaceObject f : jpl.fetchFireballs(12)) {
            byKey.putIfAbsent(key(f), f);
        }

        final List<SpaceObject> merged = new ArrayList<>(byKey.values());
        log.log(String.format("== catalog ready: %d tracked objects ==", merged.size()));
        return merged;
    }

    /** Short pause between source requests to stay polite to the hosts. */
    private void politeDelay() {
        try {
            Thread.sleep(1200L);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void addAll(final Map<String, SpaceObject> sink,
                        final List<SpaceObject> incoming,
                        final Log log,
                        final String source) {
        if (incoming.isEmpty()) {
            log.log("   [warn] " + source + " returned nothing (offline?)");
            return;
        }
        for (final SpaceObject s : incoming) {
            sink.putIfAbsent(key(s), s);
        }
        log.log("   [ok] " + source + " -> " + incoming.size() + " records");
    }

    /** Stable dedupe key across sources. */
    private String key(final SpaceObject s) {
        return s.source() + "|" + s.type() + "|" + s.id();
    }
}