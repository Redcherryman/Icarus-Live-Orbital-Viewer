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
package com.icarus_ov;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Plain launcher entry point. This class does NOT extend {@code Application},
 * which keeps the JavaFX runtime on the classpath (instead of the module path)
 * without throwing the "JavaFX runtime components are missing" error.
 *
 * @author Aiden J.S. Scoggins
 */
public final class Launcher {

    /** Private ctor: utility entry class. */
    private Launcher() {
    }

    /**
     * Program entry point that delegates to the JavaFX application launcher.
     *
     * @param args command-line arguments passed through to the application
     */
    public static void main(final String[] args) {
        IcarusOvApplication.launch(args);
    }
}