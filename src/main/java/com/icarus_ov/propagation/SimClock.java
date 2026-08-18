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

import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

import java.util.Date;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * A controllable simulation clock. By default it tracks the wall clock, but it
 * supports pausing and time-warp factors (2x, 10x, ...) so the orbit viewer can
 * speed up or freeze orbital motion while the UI keeps running.
 *
 * @author Aiden J.S. Scoggins
 */
public final class SimClock {

    private volatile boolean paused;
    private volatile double speed = 1.0;
    private volatile long baseReal = System.currentTimeMillis();
    private volatile long baseSim = baseReal;

    /** Current simulation instant as an Orekit UTC date. */
    public AbsoluteDate now() {
        return new AbsoluteDate(new Date(simMillis()), TimeScalesFactory.getUTC());
    }

    /** Sets the time-warp factor (1.0 = real time, 0 = frozen). */
    public void setSpeed(final double factor) {
        sync();
        this.speed = factor;
        this.paused = factor == 0.0;
    }

    /** Pauses (freezes simulation time) or resumes. */
    public void setPaused(final boolean value) {
        sync();
        this.paused = value;
    }

    public boolean isPaused() {
        return paused;
    }

    /** Current time-warp factor. */
    public double speed() {
        return speed;
    }

    /** Returns to real-time, unpaused. */
    public void reset() {
        sync();
        this.speed = 1.0;
        this.paused = false;
    }

    private void sync() {
        final long real = System.currentTimeMillis();
        this.baseSim = simMillis(real);
        this.baseReal = real;
    }

    private long simMillis() {
        return simMillis(System.currentTimeMillis());
    }

    private long simMillis(final long real) {
        return paused
                ? baseSim
                : baseSim + (long) ((real - baseReal) * speed);
    }
}