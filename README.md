# KUROTSUKI (黒月) — Premium Anime & Manga Sanctuary

<p align="center">
  <img src="./assets/brand/kurotsuki-logo.jpg" alt="KUROTSUKI Logo" width="160" style="border-radius: 20px; box-shadow: 0 8px 32px rgba(244, 63, 94, 0.35);" />
</p>

<p align="center">
  <strong>The definitive sanctuary for discovering, tracking, streaming, and reading anime & manga.</strong>
</p>

<p align="center">
  <a href="https://github.com/Arikisitu/sahasra/releases/tag/v0.1.0">
    <img src="https://img.shields.io/badge/Download_APK-v0.1.0_Debug-818CF8?style=for-the-badge&logo=android&logoColor=black" alt="Download APK" />
  </a>
  <img src="https://img.shields.io/badge/Anime_Engine-Anikku-818CF8?style=for-the-badge&logo=crunchyroll&logoColor=white" alt="Anikku Anime Engine" />
  <img src="https://img.shields.io/badge/Manga_Engine-AppFuton%2FFuton-FEA628?style=for-the-badge" alt="AppFuton Engine" />
  <img src="https://img.shields.io/badge/Theme-Kurotsuki_Blood_Moon-F43F5E?style=for-the-badge" alt="Kurotsuki Theme" />
  <img src="https://img.shields.io/badge/Status-Verified_&_Tested-818CF8?style=for-the-badge" alt="Status" />
</p>

---

## ✦ Overview

**KUROTSUKI (黒月)** is an ultra-fast, aesthetics-first platform for discovering, streaming, and reading anime and manga. Powered by twin flagship open engines:
- **[Anikku](https://anikku-app.github.io/)**: Dedicated anime streaming hub with smart source recommendations, panorama covers, latest episode releases, and multi-catalogue HLS resolvers.
- **[AppFuton/Futon](https://github.com/AppFuton/Futon)**: High-performance Onyx Gold manga hub with 18:13 media cards, triple-mode reader (Webtoon continuous scroll, Paged RTL, Paged LTR), and MangaDex At-Home page resolution.

### ✨ Key Capabilities
- **Anikku Anime Hub & One-Click Player (Anikku Replica)**:
  - **Anikku Design Tokens**: Canvas Dark (`#0A0D14`), Surface (`#101522`), and Electric Iris (`#818CF8`) active accents.
  - **Anime Suggestions**: Smart algorithm surfacing source-website recommendations and community picks.
  - **Panorama Cover Spotlight**: Full-width panorama artwork with one-click **"WATCH NOW • EP 1"** action.
  - **Latest Episode Updates Feed**: Chronological updates with `Ep X`, `1080p HD`, `SUB / DUB` badges, and relative timestamps.
  - **Extension Catalogues**: Live source switcher (`AllAnime HQ`, `Gogoanime CDN`, `HiAnime / Zoro`, `AnimePahe`, `Yūzōnō Vault`) with latency monitoring.
- **Futon Manga Hub & Triple-Engine Reader (AppFuton Replica)**:
  - **Onyx Gold Aesthetic**: Velvet Black base (`#0D0D0D`), Warm Obsidian surface (`#121110`), and radiant Manga Gold (`#FEA628`) active accents.
  - **Strict 18:13 Media Cards**: 16dp rounded corner cards with subtle Warm Clay borders and star ratings.
  - **Catalogue Source Switcher**: Seamless switching across open catalogues (`MangaDex`, `Kotatsu`, `Webtoons HQ`, `MangaSee`, `Bato.to`).
  - **Futon Reader**: Triple-engine reader supporting **Webtoon** (gapless continuous vertical strip), **Paged RTL** (traditional Japanese manga), and **Paged LTR**.
- **Dedicated Anime & Manga Discovery**: Top centered segmented switcher (**Anime | Manga**) offering separated discovery, trending feeds, seasonal schedules, and media carousels.
- **YouTube-Style Video Player**: Resolution/quality dropdown ("Auto (1080p)", "1080p", "720p", "480p", "360p"), playback speed selector (0.5x–2.0x), time/buffered scrubber, 10-second skip controls, and stream recovery on Web (HLS.js/HTML5) and Android (Media3 ExoPlayer).
- **Working Settings & Cache Management**: Working user preferences across restarts via `SharedPreferences` on Android and `localStorage` on Web, including theme toggles, default quality/speed, and real Coil disk/memory cache clearing.
- **Zero-Barrier Public Discovery**: Out-of-the-box trending anime & manga, seasonal breakdowns, airing calendars, and faceted search without mandatory authentication.
- **Anti-Piracy Compliance**: Strict creator-rights adherence. Displays official licensed outbound partners (Crunchyroll, Netflix, Hulu, MangaPlus, Viz, BookWalker) alongside embedded trailers and legal player engines.
- **Local-First Persistence**: Instant offline library and progress tracking via browser `localStorage` on Web and SQLite Room Database (`SahasraDatabase`) on Android.
- **Cross-Platform Design System**: Dark AMOLED surfaces (`#08090B`, `#111318`), electric iris accents (`#8585F7`, `#AAA7FF`), and crimson highlights (`#E64B55`) synchronized across Web and Jetpack Compose.
- **Android TV Ready**: Native remote-control D-Pad navigation, high-contrast focus rings, and 10-foot UI layouts.

---

## 🎨 Design System Tokens

| Token Name | Hex Code | Role |
| :--- | :--- | :--- |
| **App Background** | `#08090B` | Ultra-deep AMOLED canvas base |
| **Primary Surface** | `#111318` | Standard cards, sidebar, bottom navigation |
| **Elevated Surface** | `#191C22` | Modals, floating dropdowns, popovers |
| **Primary Accent** | `#8585F7` | Vibrant iris purple for CTAs and focus states |
| **Secondary Accent** | `#AAA7FF` | Soft lilac for badges, tags, and secondary indicators |
| **Primary Text** | `#F5F5F7` | High-contrast crisp white typography |
| **Secondary Text** | `#A0A3AE` | Muted cool slate for subtitles and metadata |
| **Border / Divider** | `#292C35` | Subtle structural delimitation |
| **Brand Crimson** | `#E64B55` | Accent alerts, favorite badges, live airing pulses |

---

## 📂 Monorepo Structure

```plaintext
sahasra/
├── .github/
│   └── workflows/
│       └── ci.yml               # Automated CI for Web, API, and Android
├── assets/
│   └── brand/
│       └── sahasra-logo.jpg     # Official SAHASRA lotus brand mark
├── docs/
│   ├── architecture.md          # Multi-tier architecture and component diagrams
│   ├── api-providers.md         # AniList, Jikan, and Kitsu provider matrix
│   ├── deployment.md            # Cloudflare & Node.js deployment guides
│   ├── release-readiness.md     # Independent verification report & test logs
│   └── security.md              # Security policies and token sanitization
├── packages/
│   ├── shared-types/            # Canonical domain models (Anime, Manga, User)
│   └── api-contracts/           # Standardized request/response DTOs & endpoints
├── apps/
│   ├── web/                     # React 18 + Vite 6 + Tailwind CSS 4 + HLS.js
│   └── android/                 # Native Android & TV (Kotlin + Jetpack Compose + Room)
├── services/
│   └── api/                     # Node.js/Express API Gateway with Circuit Breaker & TTL Cache
├── package.json                 # Monorepo workspaces definition
└── tsconfig.base.json           # Shared strict TypeScript configuration
```

---

## 🚀 Quickstart & Verification

### 1. Install Monorepo Dependencies
```bash
npm install
```

### 2. Build Core Types & Contracts
```bash
npm run build --workspace=packages/shared-types
npm run build --workspace=packages/api-contracts
```

### 3. Run Backend API Tests
All 27 test cases test circuit breakers, cache TTL, provider failover, and rate limiting:
```bash
npm run test --workspace=services/api
```

### 4. Build Web Application
```bash
npm run build --workspace=apps/web
# Production bundle built to apps/web/dist
```

### 5. Build & Test Android App
Requires Java 17 and Android SDK 35:
```bash
cd apps/android
./gradlew.bat testDebugUnitTest
./gradlew.bat assembleDebug
```
- **Generated APK**: `kurotsuki-v0.1.0-debug.apk` / `sahasra-v0.1.0-debug.apk` (Download via [GitHub Releases](https://github.com/Arikisitu/sahasra/releases/tag/v0.1.0))
- **APK Size**: `22,304,350` bytes (~21.2 MB)
- **SHA-256 Checksum**: `035FBCBC509EC54F1F4D198191B6A1AEC0C7F512564A33A027112A27497967A1`

---

## 🧪 Test Summary

| Target | Test Suite | Result | Details |
| :--- | :--- | :--- | :--- |
| **API Gateway** | Vitest v1.6.1 | **27 / 27 Passed** | Cache, Circuit Breaker, AniList/Jikan adapters, Orchestrator |
| **Web App** | TypeScript & Vite Build | **Passed** | 0 type errors, production bundle generated in 5.2s |
| **Android Client**| Gradle Unit Tests | **Passed** | 26 Gradle tasks executed / up-to-date |
| **Shared Packages**| TypeScript Check | **Passed** | 0 errors in `@sahasra/shared-types` & `@sahasra/api-contracts` |

---

## 📜 License
SAHASRA is proprietary software. All rights reserved.
