# Media Assets Guide

Provide two visual aids for the project README:

1. **Static Screenshot** (`docs/images/screenshot.png`)
   - Capture after successful login with tokens displayed.
   - Window size ~1000x700 for readability.
   - Ensure no real/secret tokens: you can replace parts of tokens with `xxxxx`.
   - Suggested redactions: middle section of access/id/refresh tokens.

2. **Animated GIF** (`docs/images/login-flow.gif`)
   - Show sequence: Launch app -> Click Login -> Browser opens -> Authenticate -> App shows tokens.
   - Keep under ~4 MB (optimize with 8-16 fps, resize width <= 900px).

## Recommended Tools (Windows)
- Screenshot: Snipping Tool or ShareX
- GIF Recording: ScreenToGif (trim + optimize)

## Workflow
1. Record raw capture (ScreenToGif) at native resolution.
2. Trim excess idle frames.
3. Apply optimization (palette reduction + dithering if needed).
4. Export `login-flow.gif`.
5. Sanitize tokens in screenshot (edit with simple image editor if needed).
6. Place final files in `docs/images/`.
7. Commit and push.

## Licensing Note
Ensure any displayed realm/client names are generic (e.g., `demo-realm`, `desktop-client`). Avoid personal or proprietary data.

## Optional Extras
- Add a second screenshot showing configuration file example.
- Add a Mermaid diagram in the main README if desired.

After adding assets, the README section will automatically render them.
