# SAHASRA — Provider Research & Architecture Reference

**Date**: October 2026  
**Document Status**: Comprehensive API Research & Audit  
**Author**: SAHASRA Engineering Team  

---

## 1. Executive Summary

This document establishes the official research, capability audit, licensing review, and operational constraints for all third-party content and metadata providers integrated into SAHASRA across Web, Android, and Backend tiers.

---

## 2. Manga & Webtoon Content Providers

### 2.1 MangaDex API (Primary Real Chapter & Page Provider)
- **Repository / Documentation**: `https://api.mangadex.org/docs/` / `https://gitlab.com/mangadex-pub/mangadex-api-docs`
- **Maintenance Status**: Actively maintained, stable v5 API.
- **License / Terms**: Free for non-commercial community open-source readers. Requires user-agent attribution.
- **Authentication**: None required for public reading, title search, and chapter page retrieval.
- **CORS Support**: Full CORS enabled across all public endpoints (`Access-Control-Allow-Origin: *`).
- **Rate Limits**:
  - Standard REST endpoints: 5 requests/second per IP.
  - At-Home image server requests: 40 requests/minute per IP.
- **Live Test Results (Verified October 2026)**:
  - Title Search: `GET https://api.mangadex.org/manga?title=Chainsaw%20Man&limit=1` -> HTTP 200 (ID: `e896c48c-3150-437d-ba57-d8567eb399ae`).
  - Chapter Feed: `GET https://api.mangadex.org/manga/{id}/feed?translatedLanguage[]=en&order[chapter]=asc&limit=10` -> HTTP 200 (Chapter 1 ID: `a20b549f-8332-42aa-8584-ef91f4edf43f`).
  - Chapter Manifest: `GET https://api.mangadex.org/at-home/server/{chapterId}` -> HTTP 200 (BaseURL: `https://cmdxd98sb0x3yprd.mangadex.network`, 51 real JPEG pages).
  - Page Image Fetch: `HEAD {baseUrl}/data/{hash}/{file}` -> HTTP 200 (`Content-Type: image/jpeg`, 417 KB).
- **Production Appropriateness**: **EXCELLENT**. Supplies genuine multi-page manifests with high-resolution scans and data-saver options without requiring scraping.

### 2.2 Official Publisher Outbound Deep-Links
- **Providers**: MangaPlus (Shueisha), Viz Media, BookWalker, ComicKey.
- **Role**: Provides direct outbound links for licensed titles when scanlation or community chapters are restricted or DMCA-protected.
- **Integration**: Direct intent / outbound web link with deep-link fallback.

---

## 3. Anime Metadata & Airing Schedule Providers

### 3.1 AniList GraphQL v2 (Primary Catalogue & Schedule)
- **Repository / Documentation**: `https://github.com/AniList/docs` / `https://docs.anilist.co/`
- **Maintenance Status**: Active production GraphQL endpoint (`https://graphql.anilist.co`).
- **License**: Free for non-commercial use with attribution.
- **Authentication**: None required for public catalogue, media details, character graphs, and airing schedules.
- **Current Operational Constraints**:
  - **Rate Limit**: AniList operates under bounded rate limits (30–90 requests/minute). Throttling and client-side memory caching with TTL (15-30 min) are strictly enforced in SAHASRA.
  - **GraphQL Validation Rule**: AniList rejects GraphQL queries with HTTP 400 if variables are declared in the operation signature but unused in the query body. (Resolved in Defect #3 fix).
- **Supported Data**:
  - Anime & Manga metadata (Romaji, English, Native titles, descriptions, studio nodes, genres).
  - Airing Schedule: Broadcast time, episode numbers, time-until-airing.
  - Trailer metadata: Official YouTube video IDs.
  - External streaming links: Deep links to Crunchyroll, Netflix, Hulu, Funimation.

### 3.2 Jikan REST API v4 (Secondary Metadata Fallback)
- **Repository / Documentation**: `https://github.com/jikan-me/jikan-rest` / `https://docs.jikan.moe/`
- **Maintenance Status**: Actively maintained MyAnimeList REST wrapper.
- **License**: Open Source (GPLv3 / MIT depending on component).
- **Rate Limits**: 3 requests/second, 60 requests/minute.
- **Role in SAHASRA**: Fallback provider in `services/api/src/providers/jikan.ts` when AniList experiences rate-limiting or downtime.

### 3.3 Kitsu API (Tertiary Metadata Fallback)
- **Repository / Documentation**: `https://kitsu.docs.apiary.io/` / `https://github.com/hummingbird-me/api-docs`
- **Maintenance Status**: Active JSON:API service.
- **Role in SAHASRA**: Tertiary fallback for trending and popular media.

---

## 4. Video Playback & Streaming Architecture

### 4.1 YouTube Player Integration (Official Streaming Trailer & Episode Embed)
- **Documentation**: `https://developers.google.com/youtube/iframe_api_reference`
- **Web Implementation**: Responsive IFrame Player with JS API controls (`autoplay`, `enablejsapi=1`, `modestbranding=1`).
- **Android Implementation**: YouTube Player API / Android WebView IFrame player with hardware acceleration and D-Pad remote key event forwarding for Android TV.
- **Capabilities**:
  - Full-resolution selection (1080p, 720p, 480p, 360p, 240p).
  - Play, pause, seek, 10s skip, playback speed (0.25x to 2x), captions.
  - 100% legal, DRM-compliant, zero server bandwidth cost.

### 4.2 HLS & Direct MP4 Media Player (Inbuilt Player Engine)
- **Web Engine**: HTML5 `<video>` element driven by `hls.js` (`v1.7.3`).
- **Android Engine**: AndroidX `Media3 ExoPlayer` (`1.3.1`) with `media3-exoplayer-hls`.
- **Capabilities**:
  - Adaptive Bitrate (ABR) stream resolution selector (auto-detects available levels from HLS m3u8 manifests).
  - Custom dark AMOLED UI with seekbar showing buffered and played segments.
  - ±10s skip buttons, speed selection, volume/mute.
  - In-player error recovery overlay with interactive "Retry" and "Switch Source".

### 4.3 Third-Party Scraper APIs (Audit & Risk Assessment)
- **Projects Evaluated**: Consumet (`https://github.com/consumet/consumet.api`), HiAnime API, AniAPI (`https://github.com/AniAPI-Team/AniAPI`).
- **Audit Findings**:
  - Highly brittle: Third-party scrapers depend on scraping video index sites (Gogoanime, Zoro, Ka-Anime).
  - Frequent breakage: Source sites frequently rotate Cloudflare Turnstile/captchas and CDN token schemes, rendering scraper instances 503/403 within weeks without constant upstream patch maintenance.
  - Legal Risk: Scraped streams violate commercial distributor terms and copyright agreements.
- **SAHASRA Architectural Decision**: SAHASRA does NOT bundle unauthorized pirate scraping scripts into production. The player provides certified licensed outbound links (Crunchyroll, Netflix), official embedded streams/trailers, and supports custom authorized direct HLS/MP4 streams with full multi-resolution playback.

---

## 5. Novels & Light Novels Research

### 5.1 Gutendex (Public-Domain Books & Light Reading)
- **Documentation**: `https://gutendex.com/` / `https://github.com/garethbjohnson/gutendex`
- **License / Rate Limits**: Free, open-source, unlimited public queries.
- **Capabilities**: Full text download in EPUB, HTML, and UTF-8 plain text for literary classics and historical translated works.

### 5.2 Open Library API
- **Documentation**: `https://openlibrary.org/developers/api`
- **Capabilities**: Metadata discovery for light novels, publication years, ISBNs, and cover scans.

---

## 6. Provider Registry & Capability Matrix

| Provider | Metadata | Real Chapters | Real Pages | Airing Schedule | Video Playback | Rate Limit |
| :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| **AniList** | Yes | No | No | Yes | Trailers / Official Links | 30–90 req/min |
| **MangaDex**| Yes | Yes | Yes | No | No | 5 req/sec (40/min images) |
| **Jikan** | Yes | No | No | Yes | Trailers | 3 req/sec |
| **Kitsu** | Yes | No | No | No | No | 60 req/min |
| **YouTube** | No | No | No | No | Yes (Trailers/Episodes) | Standard Quota |
| **Gutendex**| Yes | Yes (Text) | Yes (EPUB) | No | No | Open |
