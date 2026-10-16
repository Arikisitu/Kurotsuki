# SAHASRA (सहस्र) — Premium Anime & Manga Discovery Platform

<p align="center">
  <img src="./assets/brand/sahasra-logo.jpg" alt="SAHASRA Logo" width="160" style="border-radius: 20px; box-shadow: 0 8px 32px rgba(133, 133, 247, 0.25);" />
</p>

<p align="center">
  <strong>The definitive sanctuary for discovering, tracking, and celebrating anime & manga.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Status-Active_Development-8585F7?style=for-the-badge" alt="Status" />
  <img src="https://img.shields.io/badge/Platform-Web_•_Mobile_•_API-E64B55?style=for-the-badge" alt="Platforms" />
  <img src="https://img.shields.io/badge/TypeScript-5.4+-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript" />
  <img src="https://img.shields.io/badge/License-Proprietary-gray?style=for-the-badge" alt="License" />
</p>

---

## ✦ Overview

**SAHASRA** is an ultra-fast, aesthetics-first platform for discovering, streaming metadata tracking, reading progress tracking, and cataloging anime and manga. Driven by a unified edge API powered by AniList GraphQL, SAHASRA pairs high-performance client rendering with modern fluid design tokens.

### ✨ Highlights
- **Zero-Barrier Public Discovery**: Out-of-the-box anime and manga trending lists, seasonal breakdowns, character graphs, and advanced faceted search without mandatory authentication.
- **Unified Domain Models**: Strictly typed TypeScript schemas (`@sahasra/shared-types`) shared across web, edge worker microservices, and mobile serialization layers.
- **Deep AniList Integration**: Native support for AniList GraphQL v2 with intelligent edge caching, resilience fallbacks, and rate-limit mitigation.
- **Cinema-Grade Dark Aesthetic**: Handcrafted UI design tokens with obsidian surfaces (`#08090B`, `#111318`), electric iris accents (`#8585F7`, `#AAA7FF`), and crimson highlights (`#E64B55`).
- **Omni-Channel Architecture**: Seamless experience across modern desktop/mobile web (React + Vite + Tailwind CSS), Native Android (Kotlin + Jetpack Compose), and Edge Microservices (Cloudflare Workers / Express).

---

## 🎨 Design System Tokens

SAHASRA’s visual identity represents deep midnight aesthetics with subtle ambient luminescence:

| Token Name | Hex Code | Role |
| :--- | :--- | :--- |
| **App Background** | `#08090B` | Canvas base, ultra-deep obsidian |
| **Primary Surface** | `#111318` | Standard cards, header/sidebar navigation |
| **Elevated Surface** | `#191C22` | Modals, floating dropdowns, popovers |
| **Primary Accent** | `#8585F7` | Vibrant iris purple for CTAs and highlights |
| **Secondary Accent** | `#AAA7FF` | Soft lilac for badges, tags, and subtle glow |
| **Primary Text** | `#F5F5F7` | High-contrast crisp white typography |
| **Secondary Text** | `#A0A3AE` | Muted cool slate for subtitles and metadata |
| **Border / Divider** | `#292C35` | Subtle structural delimitation |
| **Brand Crimson** | `#E64B55` | Accent alerts, favorite badges, live airing pulses |

---

## 📂 Monorepo Architecture

```plaintext
sahasra/
├── .github/
│   └── workflows/
│       └── ci.yml               # Comprehensive lint, typecheck, build, and test CI
├── assets/
│   └── brand/
│       └── sahasra-logo.jpg     # Official brand logo artifact
├── docs/
│   ├── architecture.md          # Multi-tier system architecture and data flows
│   ├── api-providers.md         # Provider matrix, capabilities, and rate-limits
│   ├── deployment.md            # Edge Workers & Web App deployment guides
│   └── security.md              # Security policies, token storage, and sanitization
├── packages/
│   ├── shared-types/            # Canonical domain models (Anime, Manga, User, etc.)
│   └── api-contracts/           # Standardized request/response DTOs & endpoints
├── apps/                        # Application implementations (Web, API, Mobile)
│   ├── web/                     # React + Vite + Tailwind CSS Frontend
│   └── api/                     # Cloudflare Workers / Express Edge Gateway
├── .env.example                 # Environment variable templates
├── .gitignore                   # Monorepo-wide ignore rules
├── package.json                 # Monorepo workspaces definition
└── tsconfig.base.json           # Shared strict TypeScript configuration
```

---

## 🚀 Getting Started

### Prerequisites
- **Node.js**: `v18.0.0` or higher (Node 20+ / 24+ recommended)
- **npm**: `v9.0.0` or higher

### 1. Clone & Install Dependencies
```bash
git clone https://github.com/sahasra/sahasra.git
cd sahasra
npm install
```

### 2. Configure Environment
```bash
cp .env.example .env
```
*Note: Public anime/manga browsing works out of the box with public AniList GraphQL endpoints; no API keys are required for initial exploration.*

### 3. Build Shared Packages
```bash
npm run build
```

### 4. Run Type Checks & Tests
```bash
npm run typecheck
npm run test
```

---

## 🧩 Shared Packages

### `@sahasra/shared-types`
Core TypeScript typings representing the unified domain models:
- **`Anime`**: Comprehensive media metadata, episodes, airing schedule, studios, and genres.
- **`Manga`**: Serialization data, chapters, volumes, formats, and staff.
- **`Media`**: Base shared metadata, cover images, banners, and external IDs (MAL, AniList, Kitsu).
- **`User`**: User profile, watchlists, reading lists, custom scoring, and progress tracking.
- **`Provider`**: Provider capabilities, error handling, rate limiting, and status states.
- **`Common`**: Pagination, sorting, filter options, and search query parameters.

### `@sahasra/api-contracts`
Standardized API contracts and contract definitions for cross-tier type safety:
- **`ApiEndpoints`**: Exhaustive catalogue of REST / RPC endpoints.
- **`Requests`**: Request payloads, query parameters, and mutations.
- **`Responses`**: Standard envelope (`ApiResponse<T>`), error details, and pagination wrappers.

---

## 🛡️ Security & Best Practices
- **No Hardcoded Secrets**: All authentication keys and tokens are resolved via environment secrets or Cloudflare bindings.
- **Zero Leakage**: OAuth tokens and user credentials are sanitized before leaving the server perimeter.
- **Type Safety**: End-to-end type safety guaranteed through strict TypeScript configuration across all workspaces.

---

## 📜 License
SAHASRA is proprietary software. All rights reserved.
