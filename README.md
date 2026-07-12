# ShieldUp — Personal Safety & Security Toolkit

ShieldUp is a five-module personal safety app: verify suspicious calls, check
suspicious links, run a device security checkup, check your clipboard before
pasting, and scan your home WiFi for unrecognized devices. Built for personal
use, with an explicit design principle: **every check here is either a
manual, user-initiated scan, or a real Android system API — never a passive
background scan of other apps.**

## Why that principle matters

Android deliberately blocks apps from monitoring other apps' background
activity, reading other apps' data (cookies, keyboard dictionaries, message
content), or detecting "hacking" in progress — there's no OS API for any of
that, on purpose, because that capability is what spyware does. An app
claiming to do those things is either lying or is itself something to be
suspicious of. ShieldUp is built entirely within what's actually possible,
which is still genuinely useful.

## Modules

### 1. Verify a Call
Set a private family codeword (shared in person, never over text/call). When
a call claims to be from family and asks for urgent money, work through a
6-item red-flag checklist. The codeword is **never shown on screen** during
this flow — only a reminder to ask for it from memory, so it can't leak even
if someone can see your screen during the call.

### 2. Check a Link
Paste or share a suspicious link or message. Runs pattern-based phishing
heuristics (suspicious TLDs, URL shorteners, urgency language, lookalike
domains, raw IP addresses). Presented as advisory signals, not a definitive
verdict — no local tool can reliably classify a URL as malicious offline.

### 3. Security Checkup
Real checks using legitimate Android APIs:
- Screen lock status
- USB Debugging / Developer Options status
- Unknown Sources (sideloading) status
- WiFi connection awareness
- Root/jailbreak heuristic (best-effort file-path check, not a guarantee)
- One-tap link to Android's own Privacy Dashboard (which already tracks
  camera/mic/location access per app far better than a third-party app could)

### 4. Clipboard Check
A manual, on-demand check of your current clipboard content — flags if it
looks like a password or card number before you paste it somewhere public.

### 5. Home WiFi Scan
Pings your own local subnet and reads the on-device ARP table to list devices
on your home network. Label known devices; unrecognized ones are flagged.
Includes a one-tap shortcut to your router's real admin page — actual
blocking or password changes happen there, on real router credentials, never
faked in-app (spoofing/deauthing devices without real authorization would be
an attack technique, not a feature).

## Tech Stack

- Kotlin, Jetpack Compose + Material 3, Navigation Compose
- Coroutines for the parallel subnet ping sweep
- SharedPreferences for all local storage (fully offline except the WiFi scan
  module, which only talks to devices on your own local network)

## Project Structure

```
app/src/main/java/com/tahirabbas/shieldup/
├── MainActivity.kt
├── data/
│   ├── FamilyContact.kt / FamilyContactRepository.kt
│   ├── CodewordRepository.kt
│   ├── RedFlag.kt / SuspiciousCallEntry.kt / CallLogRepository.kt
│   └── KnownDeviceRepository.kt
├── utils/
│   ├── LinkCheckHelper.kt
│   ├── SecurityCheckHelper.kt
│   ├── ClipboardCheckHelper.kt
│   └── WifiScanHelper.kt
├── navigation/
│   └── NavGraph.kt
└── ui/
    ├── theme/
    └── screens/
        ├── HomeScreen.kt
        ├── VerifyCallScreen.kt / CodewordSetupScreen.kt / FamilyContactsScreen.kt / CallLogScreen.kt
        ├── LinkCheckerScreen.kt
        ├── SecurityCheckupScreen.kt
        ├── ClipboardCheckScreen.kt
        ├── WifiScannerScreen.kt
        └── SettingsScreen.kt
```

## Running it

1. Open the project root in Android Studio, let Gradle sync.
2. Run on a **physical device** for the WiFi Scanner module — emulators
   don't have a real local network to scan.
3. Set your family codeword and add contacts via Settings before testing
   the Verify Call flow.
