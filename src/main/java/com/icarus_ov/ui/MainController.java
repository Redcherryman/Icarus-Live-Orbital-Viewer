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
package com.icarus_ov.ui;

import com.icarus_ov.IcarusOvApplication;
import com.icarus_ov.data.DataService;
import com.icarus_ov.fx3d.LiveViewport;
import com.icarus_ov.model.SpaceObject;
import com.icarus_ov.model.SpaceObjectType;
import com.icarus_ov.model.TrackPoint;
import com.icarus_ov.propagation.PropagationEngine;
import com.icarus_ov.propagation.SimClock;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Assembles the retro-futurist (1980s-90s CRT) interface: title bar up top,
 * central 3D viewport with a side tracking panel, telemetry console at the
 * bottom, and an author/license footer.
 * <p>
 * Live public feeds are loaded asynchronously off the FX thread and backed by
 * the bundled offline catalog so the viewport is never empty.
 *
 * @author Aiden J.S. Scoggins
 */
public final class MainController {

    /** Author + license footer. Required on every build of the app. */
    private static final String FOOTER_TEXT = "ICARUS-OV :: Aiden J.S. Scoggins";
    private static final String FOOTER_LICENSE =
            "(c) 2026 Aiden Joshua-Steven Scoggins "
            + ":: Licensed under the Apache License, Version 2.0 "
            + "(http://www.apache.org/licenses/LICENSE-2.0)";

    private final DataService dataService = new DataService();

    private final ObservableList<SpaceObject> catalog = FXCollections.observableArrayList();
    private final FilteredList<SpaceObject> shown;
    private final Map<SpaceObjectType, CheckBox> filters =
            new EnumMap<>(SpaceObjectType.class);

    private final ListView<SpaceObject> objectList = new ListView<>();
    private final TextArea logArea = new TextArea();
    private TextField searchField;

    // Telemetry readouts (key -> value Label), refreshed on selection.
    private final Map<String, Label> telemetry = new java.util.LinkedHashMap<>();

    // Status lines produced by the worker thread, flushed to the console.
    private final java.util.concurrent.CopyOnWriteArrayList<String> loadLog =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    private StackPane viewport;
    private Label statusChip;

    // Live SGP4 engine: computes positions/altitudes from the loaded TLEs.
    private final PropagationEngine engine = new PropagationEngine();

    // Controllable simulation clock (pause / time-warp).
    private final SimClock simClock = new SimClock();

    // Retro 3D viewport (attached to the centre pane during build()).
    private LiveViewport liveViewport;

    /** Main constructor sets up a filtered, type-aware backing list. */
    public MainController() {
        this.shown = new FilteredList<>(catalog, this::passesFilters);
    }

    /** Builds and returns the fully styled root node. */
    public Parent build() {
        final BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildCenter());
        root.setBottom(buildBottom());

        this.objectList.setItems(shown);
        this.objectList.setCellFactory(v -> new ObjectCell());
        this.objectList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> {
                    updateTelemetry(newV);
                    if (liveViewport != null) {
                        liveViewport.setSelected(newV);
                    }
                });

        return root;
    }

    /** Called once the stage is shown; kicks off the live data load. */
    public void afterShow() {
        if (liveViewport != null) {
            liveViewport.start();
        }
        loadLiveData();
    }

    /** The central viewport container (3D scene attaches here in later phases). */
    public StackPane getViewport() {
        return viewport;
    }
/** Title / status bar. */
    private HBox buildTopBar() {
        final Label title = new Label("// ICARUS-OV :: ORBITAL LIVE VIEWER");
        title.getStyleClass().add("title-label");

        final Label sub = new Label("PUBLIC NASA DATA :: LIVE SGP4 TRACKING");
        sub.getStyleClass().add("title-sub");

        final HBox left = new HBox(14, title, sub);
        left.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(left, Priority.ALWAYS);

        statusChip = new Label("SYS ONLINE");
        statusChip.getStyleClass().add("status-chip");

        final HBox bar = new HBox(12, left, statusChip);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("top-bar");
        return bar;
    }

    /** Central viewport (grows) + right-hand tracking panel. */
    private HBox buildCenter() {
        this.viewport = new StackPane();
        this.viewport.getStyleClass().add("viewport");
        viewport.setCenterShape(true);

        // Attach the retro 3D live viewport (earth + neon object clouds).
        this.liveViewport = new LiveViewport(engine, shown, simClock::now);
        this.liveViewport.attach(viewport);

        final VBox side = buildSidePanel();
        side.setMinWidth(280);
        side.setPrefWidth(320);

        final HBox center = new HBox(viewport, side);
        HBox.setHgrow(viewport, Priority.ALWAYS);
        return center;
    }

    /** Right-hand panel: category filters, legend and the object list. */
    private VBox buildSidePanel() {
        final VBox panel = new VBox(8);
        panel.getStyleClass().add("side-panel");

        final Label title = new Label("-- TRACKED OBJECTS --");
        title.getStyleClass().add("panel-title");
        panel.getChildren().add(title);

        // Live name/NORAD search.
        this.searchField = new TextField();
        searchField.setPromptText("SEARCH NAME / NORAD ID");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, o, n) ->
                shown.setPredicate(this::passesFilters));
        panel.getChildren().add(searchField);

        // Time-warp controls: pause / play and speed factors.
        panel.getChildren().add(buildTimeRow());

        final VBox filterBox = new VBox(4);
        for (final SpaceObjectType type : SpaceObjectType.values()) {
            final CheckBox cb = new CheckBox(type.displayName());
            cb.setSelected(true);
            cb.getStyleClass().add("filter-check");
            cb.selectedProperty().addListener((obs, o, n) ->
                    shown.setPredicate(this::passesFilters));
            filters.put(type, cb);
            filterBox.getChildren().add(cb);
        }
        panel.getChildren().add(filterBox);

        panel.getChildren().add(buildLegend());

        VBox.setVgrow(objectList, Priority.ALWAYS);
        panel.getChildren().add(objectList);
        return panel;
    }

    /** Time-warp row: pause/play + 1x, 2x, 10x speed buttons. */
    private HBox buildTimeRow() {
        final Button pause = new Button("PAUSE");
        pause.getStyleClass().add("time-btn");
        pause.setOnAction(e -> {
            simClock.setPaused(!simClock.isPaused());
            pause.setText(simClock.isPaused() ? "PLAY" : "PAUSE");
        });

        final Button one = new Button("1x");
        final Button two = new Button("2x");
        final Button ten = new Button("10x");
        for (final Button b : new Button[]{one, two, ten}) {
            b.getStyleClass().add("time-btn");
        }
        one.setOnAction(e -> simClock.setSpeed(1.0));
        two.setOnAction(e -> simClock.setSpeed(2.0));
        ten.setOnAction(e -> simClock.setSpeed(10.0));

        final Label label = new Label("TIME:");
        label.getStyleClass().add("tel-key");

        final HBox row = new HBox(4, label, pause, one, two, ten);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** A small color-key legend mapping categories to neon markers. */
    private HBox buildLegend() {
        final HBox legend = new HBox(12);
        legend.setAlignment(Pos.CENTER_LEFT);
        for (final SpaceObjectType type : SpaceObjectType.values()) {
            final Label swatch = new Label(type.displayName().substring(0, 1));
            swatch.setTextFill(type.markerColor());
            swatch.setStyle("-fx-font-weight: bold;");
            legend.getChildren().add(swatch);
        }
        return legend;
    }

    /** Console (bottom) + author/license footer. */
    private VBox buildBottom() {
        final VBox bottom = new VBox(buildConsole(), buildFooter());
        return bottom;
    }

    /** Author + Apache 2.0 license footer (required on every build). */
    private HBox buildFooter() {
        final Label left = new Label(FOOTER_TEXT);
        left.getStyleClass().add("footer-text");

        final Label right = new Label(FOOTER_LICENSE);
        right.getStyleClass().add("footer-license");
        right.setWrapText(true);

        final HBox footer = new HBox(16, left, right);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("footer");
        HBox.setHgrow(left, Priority.ALWAYS);
        return footer;
    }
/** Bottom telemetry console. */
    private HBox buildConsole() {
        final VBox left = new VBox(4);
        left.getStyleClass().add("telemetry");
        addTelemetryRow(left, "NAME", "TGT");
        addTelemetryRow(left, "CATALOG_ID", "----");
        addTelemetryRow(left, "TYPE", "-----");
        addTelemetryRow(left, "SOURCE", "------");
        addTelemetryRow(left, "TLE_EPOCH", "-");
        addTelemetryRow(left, "ALTITUDE", "-");
        addTelemetryRow(left, "VELOCITY", "-");
        addTelemetryRow(left, "PERIOD", "-");
        addTelemetryRow(left, "INCLINATION", "-");

        this.logArea.setEditable(false);
        this.logArea.setWrapText(true);
        this.logArea.setPrefRowCount(3);
        this.logArea.getStyleClass().add("log-area");
        HBox.setHgrow(logArea, Priority.ALWAYS);

        final HBox console = new HBox(16, left, logArea);
        console.getStyleClass().add("console");
        return console;
    }

    /** Adds a key/value telemetry readout and remembers the value Label. */
    private void addTelemetryRow(final VBox parent,
                                 final String key,
                                 final String init) {
        final Label k = new Label(">> " + key);
        k.getStyleClass().add("tel-key");
        final Label v = new Label(init);
        v.getStyleClass().add("tel-val");
        final HBox row = new HBox(10, k, v);
        parent.getChildren().add(row);
        telemetry.put(key, v);
    }

    /** Category filter + live search predicate. */
    private boolean passesFilters(final SpaceObject s) {
        final CheckBox cb = filters.get(s.type());
        if (cb != null && !cb.isSelected()) {
            return false;
        }
        final TextField sf = searchField;
        if (sf == null) {
            return true;
        }
        final String q = sf.getText().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            return true;
        }
        return s.name().toLowerCase(Locale.ROOT).contains(q)
                || s.id().toLowerCase(Locale.ROOT).contains(q);
    }

    /** Fills the telemetry console for the currently selected object. */
    private void updateTelemetry(final SpaceObject s) {
        if (s == null) {
            return;
        }
        set("NAME", s.name());
        set("CATALOG_ID", s.id());
        set("TYPE", s.type().displayName());
        set("SOURCE", s.source());
        set("TLE_EPOCH", s.properties().getOrDefault("tle_epoch_yyddd", "-"));

        final TrackPoint tp = engine.propagate(s, simClock.now());
        if (tp == null) {
            set("ALTITUDE", "n/a");
            set("VELOCITY", "n/a");
            set("PERIOD", "n/a");
            set("INCLINATION", "n/a");
            return;
        }
        set("ALTITUDE", String.format("%.1f km", tp.altitudeKm));
        set("VELOCITY", String.format("%.3f km/s", tp.speedKmPerSec));
        set("PERIOD", String.format("%.1f min", tp.periodMinutes));
        set("INCLINATION", String.format("%.2f deg", tp.inclinationDeg));
    }

    private void set(final String key, final String value) {
        final Label label = telemetry.get(key);
        if (label != null) {
            label.setText(value);
        }
    }

    /** Launches the asynchronous catalog load off the FX thread. */
    private void loadLiveData() {
        final Task<List<SpaceObject>> task = new Task<>() {
            @Override
            protected List<SpaceObject> call() {
                loadLog.clear();
                loadLog.add("SYSTEM ONLINE :: ICARUS-OV " + System.getProperty("java.version"));
                final List<SpaceObject> data = dataService.loadAll(loadLog::add);
                engine.registerAll(data);
                loadLog.add("== SGP4 propagators ready: " + engine.size() + " ==");
                return data;
            }
        };
        task.setOnRunning(e -> logArea.clear());
        task.setOnSucceeded(e -> {
            final List<SpaceObject> data = task.getValue();
            catalog.setAll(data);
            for (final String line : loadLog) {
                logArea.appendText(line + "\n");
            }
            statusChip.setText("TRACKING " + catalog.size());
        });
        task.setOnFailed(e -> {
            logArea.appendText("ERROR :: " + task.getException() + "\n");
        });
        new Thread(task, "icarus-data-loader").start();
    }

    /** Retro-styled list cell that colours each object by its category. */
    private static final class ObjectCell extends javafx.scene.control.ListCell<SpaceObject> {
        @Override
        protected void updateItem(final SpaceObject item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                return;
            }
            setText(String.format("[%s] %s :: %s",
                    item.type().displayName(), item.name(), item.id()));
            setTextFill(item.type().markerColor());
        }
    }
}