# ECLIPSE Android prototype

A first Android prototype for a short-video app. It includes a vertical demo feed, Media3 video playback, local like/save state, a comments sheet placeholder, and profile/create tabs.

## Build in Android Studio

1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Open this `ECLIPSE-Android` folder in Android Studio.
3. Allow Gradle to sync and download dependencies.
4. Connect your Huawei Nova 10 SE with USB debugging enabled, or create an Android emulator.
5. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Notes

- Requires Android 8.0 (API 26) or newer.
- Demo videos stream from Google's public sample-video bucket, so the feed requires internet access.
- Likes and saved state are currently in-memory only and reset when the app process is restarted.
- Uploading videos, real accounts, server-side comments, search, and a production backend are not implemented yet.
- This is an independent prototype, not a modified TikTok app and not connected to TikTok's services.
