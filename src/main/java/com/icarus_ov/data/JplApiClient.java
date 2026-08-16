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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icarus_ov.model.SpaceObject;
import com.icarus_ov.model.SpaceObjectType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Client for NASA JPL's SSD (Solar System Dynamics) JSON API, which requires
 * no API key. Provides near-Earth asteroid close approaches and recent meteor
 * (fireball) events.
 *
 * @author Aiden J.S. Scoggins
 */
public class JplApiClient {

    /** Base host for the SSD-API services. */
    private static final String HOST = "https://ssd-api.jpl.nasa.gov";

    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    /** Builds a client with sane timeouts. */
    public JplApiClient() {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    /**
     * Fetches recent NASA-detected bolide (meteor/fireball) events.
     *
     * @param limit maximum number of events to request
     * @return fireball {@link SpaceObject} records (empty on any failure)
     */
    public List<SpaceObject> fetchFireballs(final int limit) {
        final String url = HOST + "/fireball.api?req-loc=true&limit=" + limit;
        final List<Map<String, String>> rows = parseRows(url, "fireball");
        final List<SpaceObject> out = new ArrayList<>();
        for (final Map<String, String> row : rows) {
            final String id = row.getOrDefault("id", "FB-" + out.size());
            final String date = row.getOrDefault("date", "?");
            final String energy = row.getOrDefault("energy", "?");
            out.add(new SpaceObject("FIREBALL " + date, id, SpaceObjectType.FIREBALL,
                    "NASA/JPL-SSD", null, null,
                    Map.of("date", date, "energy_kt", energy)));
        }
        return out;
    }

    /**
     * Fetches recent near-Earth asteroid close-approach entries.
     *
     * @param limit maximum number of records to request
     * @return asteroid {@link SpaceObject} records (empty on any failure)
     */
    public List<SpaceObject> fetchCloseApproaches(final int limit) {
        final String url = HOST + "/cad.api?limit=" + limit;
        final List<SpaceObject> out = new ArrayList<>();
        for (final Map<String, String> row : parseRows(url, "cad")) {
            final String des = row.getOrDefault("des", "UNK");
            final String dist = row.getOrDefault("dist", "?");
            final String date = row.getOrDefault("cd", "?");
            out.add(new SpaceObject("ASTEROID " + des, "CAD-" + des,
                    SpaceObjectType.ASTEROID, "NASA/JPL-SSD", null, null,
                    Map.of("designation", des, "date", date, "dist_lunar", dist)));
        }
        return out;
    }

    /**
     * Shared parser for the "{@code fields}" + "{@code data}" SSD response
     * shape, returning each record as a field-keyed map.
     */
    private List<Map<String, String>> parseRows(final String url, final String service) {
        final List<Map<String, String>> out = new ArrayList<>();
        try {
            final HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(12))
                    .header("User-Agent", "ICARUS-OV/0.1 (public NASA data)")
                    .GET()
                    .build();
            final HttpResponse<String> res =
                    http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                return out;
            }
            final JsonNode root = mapper.readTree(res.body());
            final JsonNode fields = root.get("fields");
            final JsonNode data = root.get("data");
            if (fields == null || data == null || !data.isArray()) {
                return out;
            }
            final List<String> names = new ArrayList<>();
            fields.forEach(f -> names.add(f.asText()));
            for (final JsonNode rec : data) {
                if (!rec.isArray()) {
                    continue;
                }
                final Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < rec.size() && i < names.size(); i++) {
                    row.put(names.get(i), rec.get(i).asText());
                }
                out.add(row);
            }
        } catch (final IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return out;
        }
        return out;
    }
}