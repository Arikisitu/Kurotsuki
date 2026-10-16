# SAHASRA Deployment Guide

## ✦ Overview

SAHASRA is architected for zero-maintenance edge deployment, minimal infrastructure cost, and ultra-low latency worldwide.

---

## 🌐 1. Web Application (`apps/web`)

The web frontend is a React + Vite + Tailwind CSS SPA that builds into pure static assets (`dist/`).

### Cloudflare Pages Deployment (Recommended)
1. Link your Git repository in the **Cloudflare Dashboard > Pages**.
2. Configure build settings:
   - **Framework preset**: Vite
   - **Build command**: `npm run build --workspace=apps/web`
   - **Build output directory**: `apps/web/dist`
   - **Root directory**: `/`
3. Set environment variables:
   - `VITE_API_BASE_URL`: Production Edge Worker URL (e.g. `https://api.sahasra.tv/api/v1`)
   - `VITE_ANILIST_GRAPHQL_ENDPOINT`: `https://graphql.anilist.co`

### Vercel Deployment
Deploy using `vercel.json` at root:
```json
{
  "buildCommand": "npm run build --workspace=apps/web",
  "outputDirectory": "apps/web/dist",
  "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }]
}
```

---

## ⚡ 2. Backend API Gateway (`apps/api`)

The backend API can be deployed as either a Cloudflare Worker (serverless edge) or a containerized Node/Express service.

### Option A: Cloudflare Workers
1. Install Wrangler CLI:
   ```bash
   npm install -g wrangler
   ```
2. Authenticate:
   ```bash
   wrangler login
   ```
3. Create KV Namespace for edge caching:
   ```bash
   wrangler kv:namespace create "SAHASRA_CACHE"
   ```
4. Deploy:
   ```bash
   cd apps/api
   wrangler deploy
   ```

### Option B: Docker / Node Server (Self-Hosted)
A standard multi-stage `Dockerfile`:
```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json tsconfig.base.json ./
COPY packages/ ./packages/
COPY apps/api/ ./apps/api/
RUN npm install
RUN npm run build --workspace=apps/api

FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/packages/ ./packages/
COPY --from=builder /app/apps/api/dist ./apps/api/dist
COPY --from=builder /app/node_modules ./node_modules
EXPOSE 4000
CMD ["node", "apps/api/dist/server.js"]
```

---

## 📱 3. Android Application (`apps/android`)

The Android application is built using Gradle, Kotlin, and Jetpack Compose.

### Release Build Steps
1. Navigate to the android directory:
   ```bash
   cd apps/android
   ```
2. Generate a release Keystore:
   ```bash
   keytool -genkey -v -keystore sahasra-release.jks -alias sahasra -keyalg RSA -keysize 2048 -validity 10000
   ```
3. Set environment variables in CI or `local.properties`:
   - `KEYSTORE_PASSWORD`: Keystore password
   - `KEY_ALIAS`: `sahasra`
   - `KEY_PASSWORD`: Key password
4. Assemble release APK / Bundle:
   ```bash
   ./gradlew assembleRelease
   ./gradlew bundleRelease
   ```
   Artifacts output to `apps/android/app/build/outputs/apk/release/` and `bundle/release/`.

---

## 📋 Environment Configuration Checklist

| Variable Name | Required | Default / Example | Purpose |
| :--- | :---: | :--- | :--- |
| `NODE_ENV` | Yes | `production` | Runtime mode |
| `VITE_API_BASE_URL` | Yes | `https://api.sahasra.tv/api/v1` | Web API client gateway |
| `ANILIST_GRAPHQL_ENDPOINT`| Yes | `https://graphql.anilist.co` | AniList upstream |
| `CACHE_TTL_SECONDS` | No | `3600` | Edge cache duration |
| `RATE_LIMIT_PER_MINUTE` | No | `90` | Upstream throttling |
