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

import org.orekit.utils.Constants;
import org.orekit.errors.OrekitException;
import org.orekit.frames.FramesFactory;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.PVCoordinates;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Wraps an Orekit {@link TLEPropagator} (SGP4) for a single object and produces
 * {@link TrackPoint} snapshots at arbitrary instants. Objects without a TLE
 * cannot be propagated and yield no snapshot.
 *
 * @author Aiden J.S. Scoggins
 */
public final class OrbitPropagator {

    private final TLEPropagator propagator;
    private final double inclinationDeg;
    private final double periodMinutes;

    /**
     * Builds a propagator from a TLE-bearing object.
     *
     * @param object the object that must carry both TLE lines
     * @throws OrekitException if the TLE is malformed
     * @throws NullPointerException if either TLE line is missing
     */
    public OrbitPropagator(final SpaceObject object) {
        OrekitInit.ensureLoaded();
        final TLE tle = new TLE(object.tleLine1(), object.tleLine2());
        this.propagator = TLEPropagator.selectExtrapolator(tle);
        this.inclinationDeg = Math.toDegrees(tle.getI());
        // NOTE: Orekit's TLE.getMeanMotion() returns RADIANS/sec, so the
        // sidereal period is 2*PI / n (seconds).
        this.periodMinutes = 2.0 * Math.PI / tle.getMeanMotion() / 60.0;
    }

    /**
     * Computes the position/velocity of the object at the given instant.
     *
     * @param date the target propagation instant
     * @return a populated {@link TrackPoint}
     */
    public TrackPoint propagate(final AbsoluteDate date) {
        final PVCoordinates pv = propagator.getPVCoordinates(date,
                FramesFactory.getTEME());
        final double radius = pv.getPosition().getNorm();
        final double altitudeKm =
                (radius - Constants.WGS84_EARTH_EQUATORIAL_RADIUS) / 1000.0;
        final double speedKmPerSec = pv.getVelocity().getNorm() / 1000.0;
        final long millis = toEpochMillis(date);
        return new TrackPoint(millis,
                pv.getPosition().getX(),
                pv.getPosition().getY(),
                pv.getPosition().getZ(),
                altitudeKm,
                speedKmPerSec,
                inclinationDeg,
                periodMinutes);
    }

    /** Converts an Orekit date to wall-clock epoch milliseconds (UTC). */
    private static long toEpochMillis(final AbsoluteDate date) {
        try {
            return date.toDate(TimeScalesFactory.getUTC()).getTime();
        } catch (final OrekitException e) {
            return date.toString().hashCode() & 0x7FFFFFFF;
        }
    }
}