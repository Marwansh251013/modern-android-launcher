# Nova Launcher

A small, modern Android home-screen launcher built with Kotlin and Jetpack Compose. Nova uses a distinct glass-and-gradient visual style, with a searchable app drawer and a customizable-feeling favorites dock.

## Features

- Can be selected as the Android Home app.
- Discovers and launches installed apps through Android's launcher intent API.
- Searchable app drawer and favorites row.
- Simple, lightweight Compose UI with no network or special permissions required.
- Refreshes the installed-app list when returning to the launcher.

## Build

Open this repository in Android Studio (Giraffe or newer), allow Gradle sync, then run the `app` configuration on an Android device or emulator (Android 8.0 / API 26+).

After installing, choose **Nova Launcher** when Android asks which Home app to use. You can change the default under Android Settings → Apps → Default apps → Home app.

## Notes

The app currently uses letter tiles instead of third-party app icon artwork and provides a focused first version rather than pretending to include unsupported widgets or advanced gesture customization. The Android system remains responsible for status/navigation bars and app launch behavior.
