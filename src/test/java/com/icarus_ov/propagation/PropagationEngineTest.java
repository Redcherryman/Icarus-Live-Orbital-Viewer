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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icarus_ov.model.SpaceObject;
import com.icarus_ov.model.SpaceObjectType;
import com.icarus_ov.model.TrackPoint;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.util.Map;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Sanity checks for the SGP4 propagation layer using a bundled ISS TLE.
 *
 * @author Aiden J.S. Scoggins
 */
class PropagationEngineTest {

    private static final String ISS_LINE1 =
            "1 25544U 98067A   26228.18012382  .00004999  00000+0  97292-4 0  9998";
    private static final String ISS_LINE2 =
            "2 25544  51.6332   3.1747 0007602  51.3505 308.8163 15.49457398581051";

    private static SpaceObject iss;

    @BeforeAll
    static void setup() {
        OrekitInit.ensureLoaded();
        iss = new SpaceObject("ISS (ZARYA)", "25544", SpaceObjectType.ISS,
                "test", ISS_LINE1, ISS_LINE2, Map.of());
    }

    @Test
    void propagatesIssToSaneLeoValues() {
        final OrbitPropagator p = new OrbitPropagator(iss);
        final AbsoluteDate now =
                new AbsoluteDate(new java.util.Date(), TimeScalesFactory.getUTC());
        final TrackPoint tp = p.propagate(now);

        assertTrue(tp.altitudeKm > 300.0 && tp.altitudeKm < 600.0,
                "ISS altitude sane: got " + tp.altitudeKm + " km");
        assertTrue(tp.speedKmPerSec > 7.0 && tp.speedKmPerSec < 8.0,
                "ISS speed sane: got " + tp.speedKmPerSec + " km/s");
        assertEquals(51.64, tp.inclinationDeg, 0.3,
                "ISS inclination roughly 51.64 deg");
        assertTrue(tp.periodMinutes > 90.0 && tp.periodMinutes < 95.0,
                "ISS period sane: got " + tp.periodMinutes + " min");
    }

    @Test
    void engineSkipsObjectsWithoutTle() {
        final SpaceObject noTle = new SpaceObject("NO-TLE", "1", SpaceObjectType.DEBRIS,
                "test", null, null, Map.of());
        final PropagationEngine engine = new PropagationEngine();
        engine.register(iss);
        engine.register(noTle);
        assertEquals(1, engine.size(), "only the TLE object is registered");
    }
}