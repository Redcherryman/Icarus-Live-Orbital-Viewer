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

import com.icarus_ov.model.SpaceObject;
import com.icarus_ov.model.SpaceObjectType;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ICARUS-OV :: Orbital Live Viewer
 * <p>
 * Client for the CelesTrak TLE catalogs (free, no API key required).
 * Fetches satellite / ISS / space-debris orbital elements as TLE text and
 * parses them into {@link SpaceObject} records ready for local propagation.
 *
 * @author Aiden J.S. Scoggins
 */
public class CelesTrakClient {

    /** Base URL for the CelesTrak group download endpoint. */
    private static final String BASE = "https://celestrak.org/NORAD/elements/gp.php";

    private final HttpClient http;
    private int limit;

    /** Builds a client with sane timeouts. */
    public CelesTrakClient() {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    /**
     * Fetches a named TLE group and returns the parsed objects.
     *
     * @param group CelesTrak group name (e.g. "stations", "visual", "debris")
     * @param type  default {@link SpaceObjectType} assigned to the whole group
     * @param cap   maximum number of objects to retain (0 = unlimited)
     * @return parsed objects for the requested group (empty on any failure)
     */
    public List<SpaceObject> fetchGroup(final String group,
                                        final SpaceObjectType type,
                                        final int cap) {
        this.limit = Math.max(cap, 0);
        final String body = get(baseUrl(group));
        if (body == null) {
            return List.of();
        }
        return parseTle(body, type);
    }
/** Builds the group download request URL. */
    private String baseUrl(final String group) {
        final String g = URLEncoder.encode(group, StandardCharsets.UTF_8);
        return BASE + "?GROUP=" + g + "&FORMAT=tle";
    }

    /** Performs a GET with retries/backoff; returns body or {@code null}. */
    private String get(final String url) {
        int backoffMs = 1500;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                final HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(25))
                        .header("User-Agent", "ICARUS-OV/0.1 (public NASA data)")
                        .GET()
                        .build();
                final HttpResponse<String> res =
                        http.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() == 200) {
                    return res.body();
                }
                // 429 / 5xx -> polite backoff, then retry.
            } catch (final IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
            try {
                Thread.sleep(backoffMs);
            } catch (final InterruptedException ie) {
                Thread.currentThread().interrupt();
                return null;
            }
            backoffMs *= 2;
        }
        return null;
    }

    /**
     * Parses a TLE catalog body (triplets of name / line 1 / line 2) into
     * {@link SpaceObject} records, honouring the configured cap.
     */
    private List<SpaceObject> parseTle(final String body, final SpaceObjectType type) {
        final List<SpaceObject> parsed = new ArrayList<>();
        if (body == null || body.isBlank()) {
            return parsed;
        }
        final String[] lines = body.replace("\r", "").split("\n");
        int count = 0;
        for (int i = 0; i + 2 < lines.length; i += 3) {
            final String name = lines[i].trim();
            final String l1 = lines[i + 1].trim();
            final String l2 = lines[i + 2].trim();
            if (!isTle(l1) || !isTle(l2)) {
                continue;
            }
            if (limit > 0 && count++ >= limit) {
                break;
            }
            final String norad = l1.length() > 6 ? l1.substring(2, 7).trim() : "?";
            final SpaceObjectType effective = resolveType(name, type, norad);
            parsed.add(new SpaceObject(name, norad, effective, "CelesTrak",
                    l1, l2, deriveProperties(l1)));
        }
        return parsed;
    }

    /** True when the line starts with the "1 " / "2 " TLE marker. */
    private boolean isTle(final String line) {
        return line != null
                && (line.startsWith("1 ") || line.startsWith("2 "));
    }

    /** Refines a group's default type based on known object identifiers. */
    private SpaceObjectType resolveType(final String name,
                                        final SpaceObjectType fallback,
                                        final String norad) {
        final String upper = name.toUpperCase(Locale.ROOT);
        if ("25544".equals(norad) || upper.contains("ISS")) {
            return SpaceObjectType.ISS;
        }
        if (upper.contains("STARLINK")) {
            return SpaceObjectType.STARLINK;
        }
        if (upper.contains("DEB")) {
            return SpaceObjectType.DEBRIS;
        }
        return fallback;
    }

    /** Derives basic HUD telemetry (epoch + mean motion) from TLE line 1. */
    private Map<String, String> deriveProperties(final String line1) {
        final Map<String, String> props = new HashMap<>();
        if (line1 == null || line1.length() < 32) {
            return props;
        }
        final String year = line1.substring(18, 20).trim();
        final String day = line1.substring(20, 32).trim();
        props.put("tle_epoch_yyddd", year + ":" + day);
        if (line1.length() > 63) {
            props.put("revs_per_day", line1.substring(52, 63).trim());
        }
        return props;
    }
}