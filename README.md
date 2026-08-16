# Icarus-OV - Orbital Live Viewer

**ICARUS-OV** is a desktop live orbital viewer powered entirely by free, public
NASA data. It renders a 3D retro-futurist Earth and the live, propagating
positions of tracked satellites, the ISS, space debris, near-Earth asteroids
and recent meteor (fireball) events.

## Author

Copyright (c) 2026 **Aiden Joshua-Steven Scoggins (Aiden J.S. Scoggins)**

Licensed under the **Apache License, Version 2.0** (the "License").
You may not use these files except in compliance with the License.
You may download, use and redistribute this software freely.

For the full license text, see [LICENSE](LICENSE).
Reference: http://www.apache.org/licenses/LICENSE-2.0
Live positions are not streamed: CelesTrak/NASA publish **TLE orbital elements**,
and ICARUS-OV turns them into live ECI positions with **SGP4** (the Apache-2.0
Orekit library). Select any object to read its live altitude, velocity, period
and inclination in the telemetry console.

## Data Sources (free, no API keys required)

| Source                     | Purpose                                          |
|----------------------------|--------------------------------------------------|
| CelesTrak TLE catalogs     | Satellites, ISS, Starlink, space debris          |
| NASA JPL / CNEOS NEO API   | Near-Earth asteroids + close approaches          |
| CNEOS Fireballs            | Recent meteor / bolide (fireball) events         |

All orbital predictions are computed locally from TLE orbital elements (SGP4).

## Copyright and Licensing

```
Copyright 2026 Aiden Joshua-Steven Scoggins (Aiden J.S. Scoggins)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```