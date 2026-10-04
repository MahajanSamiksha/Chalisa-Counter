# Chalisa Counter

A small, offline Android app for tracking a Hanuman Chalisa sadhana — 100 recitations a day across
40 days. Tap to count, and export the whole cycle to a text file when you are done.

<p align="center">
  <img src="docs/screenshot.jpeg" alt="The counter screen, showing per-day rows and overall progress" width="320">
</p>

The app is distributed as an APK shared directly with interested people, not through the Play Store.
This README is the working notes for building and sharing it.

## What the app does for its users

- **No permissions at all.** The manifest declares none. Everything is stored locally in a Room
  database on the device.
- **No network access, no analytics, no accounts.** Counts never leave the phone unless the user
  exports them.
- **Export to a text file** at any time, to a location the user picks.

## Building

Requires **JDK 17 or newer** (developed against JDK 21) and the Android SDK. The simplest route is
[Android Studio](https://developer.android.com/studio), which bundles both; open the project folder
and it syncs automatically.

```bash
./gradlew test           # run the unit tests
./gradlew assembleDebug  # build a debug APK for local testing
```

Gradle reads the SDK location from `local.properties`, which Android Studio creates. It is
gitignored because it holds a machine-specific absolute path.

The debug APK in `app/build/outputs/apk/debug/` is fine on your own device but **must not be
shared**: it is `debuggable`, and it is signed with the standard Android debug key, which is public.
Anyone could sign an APK with that key and Android would accept it as a legitimate update. Share
only release builds, per the next section.

## Project layout

Domain / data / presentation split, with dependencies pointing inward — the domain layer knows
nothing about Room or Compose.

```
app/src/main/java/com/hanumanchalisa/counter/
├── domain/         # Models, repository interfaces, use cases. Pure Kotlin, no Android deps.
├── data/           # Room database, repository implementation, file export, system clock.
├── presentation/   # ViewModel, UI state, Compose screens and theme.
└── di/             # AppContainer: manual dependency wiring, no DI framework.
```

The practice itself is configuration, not hardcoded constants: `SadhanaConfig` holds the day count
and daily target, so a 21-day cycle with a target of 50 needs a different config instance rather
than a code change.

Unit tests in `app/src/test/` cover the use cases and the ViewModel, using hand-written fakes
(`FakeChalisaCountRepository`, `FixedTimeProvider`) instead of a mocking library.

Room's exported schema lives in `app/schemas/` and is committed deliberately, so any future schema
change shows up as a reviewable diff.

## One-time signing setup

Release builds need a keystore, which is **not** in this repository. Generate one, choosing your own
password when prompted:

```bash
keytool -genkeypair -v -keystore chalisa-release.jks \
  -alias chalisa -keyalg RSA -keysize 4096 -validity 10000
```

Then copy `keystore.properties.example` to `keystore.properties` and fill in the password and alias
you chose. Both the `.jks` and `keystore.properties` are gitignored.

> **Back up the keystore and its password somewhere durable.** Android identifies an app by its
> signature. Lose the key and you cannot ship an update that installs over an existing copy —
> users would have to uninstall first, which is exactly the case where their saved counts are at
> risk. Never commit it either: with the key, anyone can sign an APK that your users' devices will
> accept as a legitimate update.

## Cutting a release

1. **Bump the version** in `app/build.gradle.kts`. `versionCode` must increase every single time —
   Android uses it, not `versionName`, to decide whether a build is an update. Ship two APKs with
   the same `versionCode` and the second will not install over the first.

2. **Build and test:**

   ```bash
   ./gradlew test assembleRelease
   ```

   The signed APK lands at `app/build/outputs/apk/release/app-release.apk`, around 8 MB. If you see
   `app-release-unsigned.apk` instead, `keystore.properties` is missing or its `storeFile` is blank —
   an unsigned APK will not install. The debug key is deliberately never used as a fallback.

3. **Rename it so versions are tellable apart:**

   ```bash
   cp app/build/outputs/apk/release/app-release.apk ~/chalisa-counter-v1.0.0.apk
   ```

   Skip this and recipients accumulate `app-release.apk`, `app-release(1).apk`, with no idea which
   is newer.

4. **Upload to Google Drive, Dropbox, or OneDrive** and share the link. Note that **Gmail blocks
   `.apk` attachments**, so send the link rather than the file — zipping it to dodge the filter just
   adds a step for the recipient.

5. **Tag the release locally** so you can reproduce exactly what you sent:

   ```bash
   git tag v1.0.0
   ```

### What to tell recipients

Something like:

> Android 7.0 or newer. Tap the link, open the downloaded file, and allow installing from an
> unknown source when asked — that prompt is normal for any app installed outside the Play Store.
> Play Protect may show a second warning because it has no reputation data for this app.
>
> The app updates only when I send a new link, so hit **Export** before reinstalling if you ever
> need to — that saves your counts to a text file.

That last point matters: the export feature is the only safety net for someone's 40-day practice if
a reinstall ever loses the app's data.

## Distribution notes

Sharing the APK without the source has some consequences worth being aware of:

- **Recipients are trusting you personally.** With no source published and no store listing, there
  is no way for anyone to verify that a devotional counter is not doing something else. That is a
  reasonable basis among friends and family; it does not extend to strangers.
- **No automatic updates**, and no notification when a new version exists.
- **F-Droid is not an option** — it requires public source and reproducible builds.
- **Default copyright applies.** Nobody may redistribute the APK without your permission, so if you
  are happy for people to forward it to friends, say so explicitly when you share it.

If you later decide to publish the source, the `LICENSE` file already grants MIT terms and this repo
is ready to push as-is. GitHub Actions workflows for testing on push and publishing signed APKs to
GitHub Releases were removed in the commit that introduced this section — recover them from git
history rather than rewriting them.

## License

The source is licensed [MIT](LICENSE) should you choose to publish it. Until then it is unpublished
and default copyright applies to the APK you distribute.
