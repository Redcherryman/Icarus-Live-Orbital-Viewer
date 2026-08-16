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

import com.icarus_ov.ui.MainController;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * JavaFX application root. Boots the retro-futurist scene assembled by
 * {@link MainController} and applies the CRT stylesheet.
 *
 * @author Aiden J.S. Scoggins
 */
public final class IcarusOvApplication extends Application {

    /** Window title shown on the OS decoration and start bar. */
    public static final String APP_NAME = "ICARUS-OV :: Orbital Live Viewer";

    /** On-screen root for the CSS just above the classpath. */
    private static final String STYLESHEET = "/css/retro.css";

    @Override
    public void start(final Stage stage) {
        Objects.requireNonNull(stage, "stage");

        final MainController controller = new MainController();
        final Parent root = controller.build();

        final Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(STYLESHEET);

        stage.setTitle(APP_NAME);
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();

        controller.afterShow();
    }

    /**
     * Programmatic launch helper (e.g. for tests). Kept for parity with
     * {@link Launcher} and to allow programmatic startup.
     *
     * @param args command-line arguments
     */
    public static void launchApp(final String... args) {
        Application.launch(IcarusOvApplication.class, args);
    }
}