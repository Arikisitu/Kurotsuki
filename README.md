# SAHASRA (सहस्र) — Premium Anime & Manga Discovery Platform

<p align="center">
  <img src="./assets/brand/sahasra-logo.jpg" alt="SAHASRA Logo" width="160" style="border-radius: 20px; box-shadow: 0 8px 32px rgba(133, 133, 247, 0.25);" />
</p>

<p align="center">
  <strong>The definitive sanctuary for discovering, tracking, and celebrating anime & manga.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Status-Verified_&_Tested-8585F7?style=for-the-badge" alt="Status" />
  <img src="https://img.shields.io/badge/Platforms-Web_•_Android_•_Android_TV_•_API-E64B55?style=for-the-badge" alt="Platforms" />
  <img src="https://img.shields.io/badge/TypeScript-5.7+-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript" />
  <img src="https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Tests-27%2F27_Passing-4ADE80?style=for-the-badge" alt="Tests" />
</p>

---

## ✦ Overview

**SAHASRA** is an ultra-fast, aesthetics-first platform for discovering, streaming metadata tracking, reading progress tracking, and cataloging anime and manga. Driven by a unified edge API powered by AniList GraphQL (with Jikan & Kitsu resilience fallbacks), SAHASRA pairs high-performance client rendering with modern fluid design tokens across Web, Android phones, and Android TV.

### ✨ Key Capabilities
- **Zero-Barrier Public Discovery**: Out-of-the-box trending anime & manga, seasonal breakdowns, airing calendars, and faceted search without mandatory authentication.
- **Inbuilt Streaming Player**: Customized AMOLED video player with HLS.js adaptive bitrate streaming (`.m3u8`), HTML5 MP4 playback, official YouTube trailers, 10s scrub skips, speed selection (0.5x–2.0x), and in-player error recovery.
- **Inbuilt Manga Reader**: Dual-mode reader engine supporting continuous vertical webtoon scrolling and single-page flip mode with keyboard shortcuts (`ArrowLeft`/`ArrowRight`/`Space`), width toggling (Normal/Wide/Full), and chapter selectors.
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
- **Generated APK**: `apps/android/app/build/outputs/apk/debug/app-debug.apk`
- **APK Size**: `21,382,299` bytes (~20.4 MB)
- **SHA-256 Checksum**: `D65097866B02DC93D990B4E9CE0BC02EB60FF3FAF839E5EF540E40C4FB5DD3F2`

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
