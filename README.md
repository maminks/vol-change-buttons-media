# VolSkip

**Skip tracks by holding the volume buttons — no root required.**

Hold **Vol+** for the next track, hold **Vol−** for the previous one. A short press still changes the volume as usual. It works with any media player (Spotify, YouTube Music, Poweramp, VLC, …), because it simply sends the standard media keys.

🇷🇺 [Читать на русском](README.ru.md)

## Features

- Long-press **Vol+** → next track, long-press **Vol−** → previous track
- Short press keeps normal volume behaviour
- Only active while music is playing — ringer and notification volume are never touched
- Adjustable hold time (200–1100 ms) and optional haptic feedback
- Tiny (~30 KB), no internet permission, no ads, no tracking
- English and Russian UI

## Install

1. Download `VolSkip.apk` from the [latest release](../../releases/latest).
2. Install it (allow installation from your browser / file manager if asked).
3. Open VolSkip and tap **Open accessibility settings**, then enable **VolSkip**.

> **GrapheneOS / Android 13+:** if the toggle is greyed out, open *Settings → Apps → VolSkip → ⋮ → Allow restricted settings* first, then enable the service.

Requires Android 12 (API 31) or newer.

## How it works

VolSkip is an [`AccessibilityService`](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService) with `canRequestFilterKeyEvents`. It sees volume key events before they reach the system:

1. On key down, if `AudioManager.isMusicActive()` is true, the event is swallowed and a timer starts.
2. If the key is held past the threshold, a `KEYCODE_MEDIA_NEXT` / `KEYCODE_MEDIA_PREVIOUS` event is dispatched through `AudioManager`.
3. If the key is released earlier, a normal `adjustStreamVolume` step is performed instead.

The service does not read the screen (`canRetrieveWindowContent="false"`) and requests no network access.

## Limitations

- **With the screen off** Android routes volume keys straight to the active media session before accessibility services see them, and behaviour differs between devices and ROMs. If it does not trigger on your phone with the display off, that is an OS-level restriction an unprivileged app cannot bypass.
  - Custom ROMs such as LineageOS / crDroid have this built in (*Settings → Buttons → Volume button music control*).
  - With root you can do it at system level via an LSPosed / Magisk module.
- While music is paused, the buttons behave normally.

## Build from source

```bash
git clone https://github.com/maminks/vol-chande-buttons
cd vol-chande-buttons
gradle assembleRelease        # or open the folder in Android Studio
```

Requirements: JDK 17, Gradle 8.10+, Android SDK 35. The output is `app/build/outputs/apk/release/app-release.apk` (signed with the debug key — fine for sideloading; use your own keystore if you redistribute).

Every push is built by GitHub Actions (`.github/workflows/build.yml`); tagged `v*` pushes publish a versioned release.

## Project layout

```
app/src/main/java/dev/qzzxcq/volskip/
├── VolSkipService.kt   # key interception + long-press logic
├── MainActivity.kt     # settings UI (hold time, vibration, service status)
└── Prefs.kt            # SharedPreferences wrapper
app/src/main/res/       # strings (en + ru), accessibility config, icon
```

## License

[MIT](LICENSE)
