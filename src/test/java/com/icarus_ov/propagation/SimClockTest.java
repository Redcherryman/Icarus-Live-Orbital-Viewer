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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.orekit.time.TimeScalesFactory;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Verifies pause and time-warp behaviour of the simulation clock.
 *
 * @author Aiden J.S. Scoggins
 */
class SimClockTest {

    private long simMillis(final SimClock c) throws Exception {
        return c.now().toDate(TimeScalesFactory.getUTC()).getTime();
    }

    @Test
    void pausedClockStaysFrozen() throws Exception {
        final SimClock c = new SimClock();
        c.setPaused(true);
        final long before = simMillis(c);
        Thread.sleep(150);
        final long after = simMillis(c);
        assertTrue(Math.abs(after - before) < 40,
                "paused clock must not advance (delta " + (after - before) + "ms)");
    }

    @Test
    void warpedClockAdvancesFasterThanReal() throws Exception {
        final SimClock c = new SimClock();
        final long realStart = System.currentTimeMillis();
        final long simStart = simMillis(c);
        c.setSpeed(2.0);
        Thread.sleep(220);
        final long simEnd = simMillis(c);
        final long realElapsed = System.currentTimeMillis() - realStart;
        final long expected = simStart + (long) (realElapsed * 2.0);
        assertTrue(Math.abs(simEnd - expected) < 120,
                "2x clock should advance ~2x real time");
    }
}