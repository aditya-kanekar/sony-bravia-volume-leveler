# Sony Bravia Volume Leveler

A small Android TV app that evens out volume on a Sony Bravia (Google TV): loud ads come down, quiet dialogue comes up. Built and tested on a **BRAVIA VU31** running Android 14.

## What it does

- Attaches Android's `DynamicsProcessing` effect to the global output mix (audio session 0): one full-band compressor followed by a safety limiter.
- Three presets: **Off**, **Light** (2.5:1 above −26 dB, +5 dB makeup) and **Strong** (5:1 above −38 dB, +12 dB makeup, −6 dB ceiling). Tuning lives in [`Presets.java`](leveler/app/src/main/java/com/adityak/leveler/Presets.java).
- The remote's **TV** button opens a small panel in the corner of the screen. **Left/Right** changes the preset, **OK/Back/TV** closes it, and it hides itself after 5 seconds. This uses an accessibility service that only listens for key presses.
- Turns itself back on after a reboot, waiting 20 seconds so the TV's audio hardware is ready first.

## Compatibility (VU31, Android 14)

| App | Plays with leveler on | Audio is leveled |
|---|---|---|
| YouTube | ✅ | ✅ verified |
| JioHotstar | ✅ | not verified |
| Prime Video | ✅ | not verified |
| Netflix, **stereo** track | ✅ | ❌ (direct hardware path) |
| Netflix, **5.1** track | ❌ freezes | ❌ |

**Netflix caveat:** Netflix's 5.1 track goes to the TV's Dolby decoder through a hardware-synced tunnel. On this TV, creating a session-0 effect even once since boot breaks that path until the next reboot. Netflix then hangs at 25% or freezes a few seconds into playback, and detaching the effect doesn't help. Choose the non-5.1 track under **Audio & Subtitles**. TV speakers are stereo anyway, so nothing is lost. If a title freezes, set the leveler to Off and restart the TV.

Other quirks found on this TV:
- Sony's `DynamicsProcessing` rejects `VARIANT_FAVOR_TIME_RESOLUTION`, so the app uses `VARIANT_FAVOR_FREQUENCY_RESOLUTION`.
- `adb shell input keyevent` presses don't reach the accessibility service, but real remote presses do.
- Reinstalling the APK switches the accessibility service off, so you have to enable it again (see below).

## Build

Requires JDK 17 and the Android SDK (platform 35).

```bash
cd leveler
echo "sdk.dir=/path/to/android/sdk" > local.properties
./gradlew assembleDebug
```

## Install on the TV

1. On the TV, go to **Settings → System → About** and select **Build** 7 times. Then turn on **Developer options → Wireless debugging**.
2. Pair once with `adb pair <ip>:<pairing-port>`, then connect with `adb connect <ip>:<port>`.
3. Install and enable the remote-button service:

```bash
adb install -r leveler/app/build/outputs/apk/debug/app-debug.apk
adb shell settings put secure enabled_accessibility_services com.adityak.leveler/com.adityak.leveler.RemoteKeyService
adb shell settings put secure accessibility_enabled 1
```

## Diagnostics

[`tools/check_audio.py`](tools/check_audio.py) lists the TV's audio outputs, shows which are playing, and shows whether the leveler is attached to each:

```bash
python3 tools/check_audio.py
```
