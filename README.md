# 🔐 Keycloak Swing (OAuth2 Authorization Code + PKCE Demo)

A Java Swing desktop demo application for educational purposes. It sends the user through a **Keycloak Authorization Code + PKCE** login, captures the browser redirect with a lightweight local HTTP server, and displays the returned tokens (access / refresh / id) in a formatted view.

## 📚 Table of Contents
- [Features](#features)
- [Architecture Flow](#architecture-flow)
- [Build](#build)
- [Run](#run)
 - [Screenshots & Demo](#screenshots--demo)
- [Configuration (config.properties)](#configuration-configproperties)
- [Short Note on PKCE](#short-note-on-pkce)
- [Security Notes](#security-notes)
- [Improvements / Roadmap](#improvements--roadmap)
- [License](#license)

## ✨ Features
- Authorization Code + PKCE flow
- State & nonce generation (CSRF / replay protection)
- Local `ServerSocket` catcher for `http://localhost:<port>/callback`
- Simple log panel (auto scroll)
- Lightweight JSON response splitting / token formatting
- Externalized parameters via `config.properties`
- Optional `clientSecret` for confidential clients (omit for public)

## 🧭 Architecture Flow
```
User (Swing UI) -> Login
 1) Generate PKCE code_verifier + code_challenge
 2) Generate state & nonce
 3) Open Keycloak authorization URL in default browser
 4) User authenticates / consents
 5) Keycloak redirect: http://localhost:8080/callback?code=...&state=...
 6) Local HTTP server (ServerSocket) extracts code + state
 7) Verify state -> POST code + code_verifier to token endpoint
 8) Receive access_token / refresh_token / id_token -> display
```

## 🛠️ Build
Prerequisites:
- Java 17 (or compatible JDK)
- Maven 3.x

Compile the project:
```powershell
mvn clean package
```
Resulting JAR:
```
./target/keycloak-swing-1.0.0.jar
```

## ▶️ Run
```powershell
java -jar target/keycloak-swing-1.0.0.jar
```
After launch press **Login**. Your browser opens; once you authenticate the tokens appear in the log panel.

## 🖼️ Screenshots & Demo
| Screenshot | Flow GIF |
|-----------|----------|
| ![App Screenshot](docs/images/screenshot.png) <br/> (SVG placeholder: `docs/images/screenshot.svg`) | ![Login Flow](docs/images/login-flow.gif) <br/> (SVG placeholder: `docs/images/login-flow.svg`) |

> Note: Tokens in the screenshot should be partially redacted for safety. GIF kept short (<4MB). See `docs/ASSETS.md` for capture guidelines.

## ⚙️ Configuration (config.properties)
Example (`src/main/resources/config.properties`):
```
keycloak.clientId=...
keycloak.clientSecret=...            # Leave empty for a public client
keycloak.redirectUri=http://localhost:8080/callback
keycloak.scope=openid
keycloak.serverUrl=https://<host>
keycloak.realmId=<realm-id>
local.server.port=8080
app.log.timestamp=true
```
Derived endpoints:
- Authorization: `${serverUrl}/realms/${realmId}/protocol/openid-connect/auth`
- Token: `${serverUrl}/realms/${realmId}/protocol/openid-connect/token`

You typically adjust:
- `keycloak.clientId`
- (If confidential) `keycloak.clientSecret`
- `keycloak.serverUrl`
- `keycloak.realmId`
- `redirectUri` (must be registered in the Keycloak client)

Add scopes (example):
```
keycloak.scope=openid profile email
```

## 🔑 Short Note on PKCE
PKCE (Proof Key for Code Exchange) strengthens Authorization Code flow for native/desktop/mobile apps that cannot safely store a client secret. `code_verifier` -> SHA256 -> Base64URL = `code_challenge`. The challenge is sent in the authorization step; the original verifier must match during the token exchange.

## 🛡️ Security Notes
- Do not commit real secrets (like `clientSecret`) for production; prefer environment variables or a secret vault.
- Token JSON is parsed naïvely here; use a robust library (Jackson / Gson) in real projects.
- Local HTTP server listens only on loopback; still handle port conflicts (could add dynamic free-port logic).
- HTTPS is not used for the localhost callback in this demo; always enforce HTTPS in production redirects.
- Consider masking / truncating tokens in logs for audits.

## 🚀 Improvements / Roadmap
Below is a living checklist. Checked items are already implemented; unchecked ones are candidates for future contributions.

### ✅ Completed
- [x] Externalized configuration via `config.properties`
- [x] Authorization Code + PKCE implementation
- [x] Basic logging panel with auto-scroll
- [x] Basic CSRF (state) & nonce generation
- [x] Token acquisition & display
- [x] Proper JSON parsing (Gson integrated)
- [x] PKCE unit tests

### 🗂️ Planned / Open
- [ ] Decode & pretty-print ID Token (JWT header + payload, redact sensitive claims)
- [ ] Add Refresh Token button (refresh_token grant flow)
- [ ] Auto-select free localhost port if configured port is busy (and adapt redirect URI)
- [ ] More robust error handling & categorized messages (network / oauth / parse)
- [ ] Token masking (only first & last N chars visible; copy-to-clipboard button)
- [ ] Multiple environment profile files (e.g. `config.dev.properties`, `config.prod.properties`)
- [ ] Maven Wrapper (`mvnw`, `mvnw.cmd`) inclusion
- [ ] CI workflow (GitHub Actions) + build/test badge in README
- [ ] CONTRIBUTING.md (PR / issue guidelines)
- [ ] CODE_OF_CONDUCT.md
- [ ] SECURITY.md (vulnerability disclosure process)
- [ ] CHANGELOG.md (Keep a Changelog format) & semantic versioning policy
- [ ] JWT claim viewer panel with collapsible sections
- [ ] Dynamic scope selection UI (multi-select before opening browser)
- [ ] Retry logic for transient HTTP errors (exponential backoff)
- [ ] Optional structured logging (JSON) to a file
- [ ] Add tests for JSON token parsing & error branches
- [ ] Refresh token auto-renew countdown / indicator
- [ ] Optional port selection dialog if bind fails
- [ ] Graceful shutdown hook for server socket

Contributions welcome—open an issue to discuss large changes before submitting a PR.

## 📄 License
This project is licensed under the **MIT License**. See: [LICENSE](LICENSE)

---
Contributions or questions are welcome via Pull Requests / Issues.
