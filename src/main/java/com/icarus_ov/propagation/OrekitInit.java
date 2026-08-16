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

import org.orekit.data.DataContext;
import org.orekit.data.DirectoryCrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * One-time Orekit bootstrap. Registers the bundled, Apache-compatible Orekit
 * data (currently the UTC-TAI leap-second history) from the classpath so that
 * UTC-based time scales and TLE parsing resolve without external downloads.
 *
 * @author Aiden J.S. Scoggins
 */
public final class OrekitInit {

    private static volatile boolean initialized;

    private OrekitInit() {
    }

    /**
     * Ensures Orekit data providers are registered exactly once.
     *
     * @return {@code true} if data was successfully registered
     */
    public static boolean ensureLoaded() {
        if (initialized) {
            return true;
        }
        synchronized (OrekitInit.class) {
            if (initialized) {
                return true;
            }
            final File dir = new File(System.getProperty("java.io.tmpdir"),
                    "icarus-orekit-data");
            if (!dir.exists() && !dir.mkdirs()) {
                return initialized = true;
            }
            final File out = new File(dir, "UTC-TAI.history");
            extract("/orekit-data/UTC-TAI.history", out);
            DataContext.getDefault().getDataProvidersManager()
                    .addProvider(new DirectoryCrawler(dir));
            return initialized = true;
        }
    }

    /** Copies a bundled data resource out to a plain file Orekit can read. */
    private static void extract(final String resource, final File target) {
        try (InputStream in = OrekitInit.class.getResourceAsStream(resource);
             OutputStream os = new FileOutputStream(target)) {
            if (in != null) {
                in.transferTo(os);
            }
        } catch (final IOException e) {
            throw new IllegalStateException("cannot extract Orekit data: " + resource, e);
        }
    }
}