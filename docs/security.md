# SAHASRA Security & Privacy Standards

## ✦ Core Security Principles

SAHASRA is committed to strict data confidentiality, resilient perimeter defense, and zero unauthorized credential persistence.

---

## 🔒 1. Credential & Secret Management
- **Zero Hardcoded Secrets**: No API keys, OAuth secrets, signing certificates, or session keys are ever committed to the repository.
- **Environment Isolation**: Production secrets are injected at runtime via Cloudflare Secrets (`wrangler secret put`), GitHub Actions Encrypted Secrets, or local `.env` files which are strictly excluded by `.gitignore`.
- **Automated Secret Scanning**: Pre-commit hooks and CI scans audit for accidental token leaks.

---

## 🌐 2. Web Security & Headers

All web responses deployed via Cloudflare Pages or the Edge Gateway enforce modern HTTP security headers:

| Header | Production Value | Purpose |
| :--- | :--- | :--- |
| **Content-Security-Policy (CSP)** | `default-src 'self'; img-src 'self' data: https://s4.anilist.co https://img.youtube.com; script-src 'self'; style-src 'self' 'unsafe-inline'; font-src 'self' https://fonts.gstatic.com;` | Mitigates Cross-Site Scripting (XSS) |
| **Strict-Transport-Security** | `max-age=31536000; includeSubDomains; preload` | Enforces HTTPS strictly |
| **X-Frame-Options** | `DENY` | Prevents Clickjacking |
| **X-Content-Type-Options** | `nosniff` | Blocks MIME type sniffing |
| **Referrer-Policy** | `strict-origin-when-cross-origin` | Limits referer leakages |

---

## 🧹 3. Input Sanitization & HTML Escaping
- **Media Synopses**: Descriptions delivered from AniList and external sources often contain raw HTML tags (`<br>`, `<i>`, `<b>`, `<a>`). SAHASRA sanitizes all HTML using DOMPurify before rendering to prevent script execution.
- **Query Parameter Validation**: API routes strictly validate numeric IDs, page limits, and enum types via `@sahasra/api-contracts` before forwarding requests to the database or upstream providers.

---

## 🛡️ 4. CORS Policy
The Edge Gateway enforces strict Cross-Origin Resource Sharing (CORS):
```typescript
const allowedOrigins = [
  'https://sahasra.tv',
  'https://app.sahasra.tv',
  'http://localhost:3000', // Local development
];
```
Wildcard `*` origins are rejected for authenticated routes.

---

## 🔑 5. OAuth & User Privacy
- **OAuth 2.0 PKCE**: User authorization with AniList or external authenticators utilizes Proof Key for Code Exchange (PKCE) for maximum mobile and SPA security.
- **Token Storage**: User bearer tokens on the web are kept in memory or secure `HttpOnly`, `SameSite=Lax`, `Secure` cookies. On Android, tokens are encrypted inside Android Keystore / EncryptedSharedPreferences.
- **No PII Collection**: SAHASRA does not collect or sell user tracking cookies or personal identifying information.
