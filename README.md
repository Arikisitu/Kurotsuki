# SAHASRA (सहस्र) — Premium Anime & Manga Discovery Platform

<p align="center">
  <img src="./assets/brand/sahasra-logo.jpg" alt="SAHASRA Logo" width="160" style="border-radius: 20px; box-shadow: 0 8px 32px rgba(133, 133, 247, 0.25);" />
</p>

<p align="center">
  <strong>The definitive sanctuary for discovering, tracking, and celebrating anime & manga.</strong>
</p>

<p align="center">
  <a href="https://github.com/Arikisitu/sahasra/releases/tag/v0.1.0">
    <img src="https://img.shields.io/badge/Download_APK-v0.1.0_Debug-FEA628?style=for-the-badge&logo=android&logoColor=black" alt="Download APK" />
  </a>
  <img src="https://img.shields.io/badge/Theme-Futon_Onyx_Gold-0D0D0D?style=for-the-badge&logo=materialdesign&logoColor=FEA628" alt="Futon Onyx Gold" />
  <img src="https://img.shields.io/badge/Manga_Engine-AppFuton%2FFuton-FEA628?style=for-the-badge" alt="AppFuton Engine" />
  <img src="https://img.shields.io/badge/Status-Verified_&_Tested-8585F7?style=for-the-badge" alt="Status" />
  <img src="https://img.shields.io/badge/Platforms-Web_•_Android_•_Android_TV_•_API-E64B55?style=for-the-badge" alt="Platforms" />
</p>

---

## ✦ Overview

**SAHASRA** is an ultra-fast, aesthetics-first platform for discovering, streaming metadata tracking, reading progress tracking, and cataloging anime and manga. Driven by a unified edge API powered by AniList GraphQL (with Jikan & Kitsu resilience fallbacks), SAHASRA pairs high-performance client rendering with modern fluid design tokens across Web, Android phones, and Android TV.

### ✨ Key Capabilities
- **Futon Manga Hub & Reader (AppFuton Replica)**: Complete implementation of the flagship [AppFuton/Futon](https://github.com/AppFuton/Futon) experience:
  - **Onyx Gold Aesthetic**: Velvet Black base (`#0D0D0D`), Warm Obsidian surface (`#121110`), and radiant Manga Gold (`#FEA628`) active accents.
  - **Strict 18:13 Media Cards**: 16dp rounded corner cards with subtle Warm Clay borders, star ratings, and Warm Ivory typography.
  - **Catalogue Source Switcher**: Seamless switching across open catalogues (`MangaDex (Official Scans)`, `Kotatsu Multi-Mirror`, `Webtoons HQ`, `MangaSee`, `Bato.to`).
  - **Dedicated Sub-Tabs**: `POPULAR`, `LATEST`, `WEBTOONS`, and `SOURCES` (Catalogue manager with live latency and health monitors).
  - **Futon Reader**: Triple-engine reader supporting **Webtoon** (gapless continuous vertical strip), **Paged RTL** (traditional Japanese manga), and **Paged LTR** with floating HUD page counters and seekbar.
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
- **Generated APK**: `sahasra-v0.1.0-debug.apk` (Download via [GitHub Releases](https://github.com/Arikisitu/sahasra/releases/tag/v0.1.0))
- **APK Size**: `21,705,357` bytes (~20.7 MB)
- **SHA-256 Checksum**: `77EF40115C64668789FDB4AF530599E672818B0845C25FE3F590555FED664897`

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
