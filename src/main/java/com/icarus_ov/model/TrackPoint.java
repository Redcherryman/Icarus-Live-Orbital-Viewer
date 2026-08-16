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

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * An immutable, time-tagged propagation snapshot for one object: an ECI
 * (Earth-Centered Inertial) position plus derived orbital scalars. Deliberately
 * framework-free (plain doubles) so the 3D viewport and HUD need no Orekit
 * types on their own.
 *
 * @author Aiden J.S. Scoggins
 */
public final class TrackPoint {

    /** Wall-clock time (milliseconds since epoch) this snapshot was taken. */
    public final long declaredAtMillis;
    /** ECI position, metres, x (toward Vernal Equinox direction). */
    public final double eciXM;
    /** ECI position, metres, y. */
    public final double eciYM;
    /** ECI position, metres, z (north polar axis). */
    public final double eciZM;
    /** Altitude above the reference ellipsoid/mean Earth radius, km. */
    public final double altitudeKm;
    /** Apparent orbital speed, km/s. */
    public final double speedKmPerSec;
    /** Orbital inclination, degrees. */
    public final double inclinationDeg;
    /** Orbital period, minutes. */
    public final double periodMinutes;

    /**
     * Full constructor.
     *
     * @param declaredAtMillis wall-clock snapshot time
     * @param eciXM ECI x position in metres
     * @param eciYM ECI y position in metres
     * @param eciZM ECI z position in metres
     * @param altitudeKm altitude in km
     * @param speedKmPerSec speed in km/s
     * @param inclinationDeg inclination in degrees
     * @param periodMinutes orbital period in minutes
     */
    public TrackPoint(final long declaredAtMillis,
                      final double eciXM,
                      final double eciYM,
                      final double eciZM,
                      final double altitudeKm,
                      final double speedKmPerSec,
                      final double inclinationDeg,
                      final double periodMinutes) {
        this.declaredAtMillis = declaredAtMillis;
        this.eciXM = eciXM;
        this.eciYM = eciYM;
        this.eciZM = eciZM;
        this.altitudeKm = altitudeKm;
        this.speedKmPerSec = speedKmPerSec;
        this.inclinationDeg = inclinationDeg;
        this.periodMinutes = periodMinutes;
    }

    @Override
    public String toString() {
        return String.format(
                "alt=%.1f km speed=%.3f km/s i=%.1f deg period=%.1f min "
                + "eci=(%.0f,%.0f,%.0f) m",
                altitudeKm, speedKmPerSec, inclinationDeg, periodMinutes,
                eciXM, eciYM, eciZM);
    }
}