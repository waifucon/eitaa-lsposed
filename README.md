# Eitaa AdBlocker

An LSPosed module to block and remove advertisements in Eitaa (`ir.eitaa.messenger`).

---

## What it does

- Removes sponsored/ad dialogs from the chat list.
- Removes pinned ad banners from channels and groups.
- Removes the floating circular ad button in channels.
- Removes sponsored messages at the bottom of channels.
- Removes popup video ads during media playback.
- Removes search ads and Explore/Trends ads.
- Disables background ad tracking and telemetry.

---

## Installation & Setup

1. Build the APK:
   ```bash
   ./gradlew assembleRelease
   ```
   The APK will be at:
   `app/build/outputs/apk/release/app-release.apk`

2. Install the APK on your device:
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

3. Open **LSPosed Manager**:
   - Go to the **Modules** tab.
   - Find **Eitaa AdBlocker** and turn it on.
   - Ensure the scope is set to **Eitaa** (`ir.eitaa.messenger`).

4. Force close and reopen Eitaa.
