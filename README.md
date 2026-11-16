# LaunchAnimCustomizer

An LSPosed module that lets you tune the spring physics of the launcher open and close animations on Pixel and AOSP based ROMs. Changes apply live, without restarting the launcher.

## Features

- Twenty ready made themes, plus your own saved themes
- Opening curves: spring, overshoot, anticipate, bounce, decelerate, ease, custom Bezier and linear
- Closing springs with stiffness and damping, optionally separate for horizontal, vertical and scale
- Speed control for both directions
- Live preview that plays your opening and closing settings
- Material 3 interface with dynamic color
- Backup and restore of your settings
- Adjustable bottom bar: floating or docked, width, height, opacity, text size, distance from bottom, hide on scroll and labels
- 18 languages that follow the system language: English, Turkish, Russian, German, French, Spanish, Italian, Portuguese, Dutch, Polish, Ukrainian, Arabic, Hindi, Indonesian, Chinese (Simplified), Korean, Japanese and Vietnamese

## Requirements

- Android 12 or newer
- LSPosed with libxposed API 101 or newer
- Root access is optional and only used for the restart buttons
- The default Pixel Launcher or the AOSP Quickstep launcher

## How it works

The module hooks the launcher process.

- Opening from an icon: the launcher drives the window with a 500 ms animator and two interpolators. The module swaps those interpolators for the curve you picked.
- Closing and return to home: `RectFSpringAnim` reads its spring values through `DynamicResource`. The module replaces the stiffness and damping values while a closing animation starts.

Settings are shared with the launcher through libxposed remote preferences, so a new value is picked up by the next animation.

## Install

1. Install the APK
2. Enable the module in LSPosed and tick your launcher in the scope
3. Restart the launcher from the Settings tab or reboot
4. Open the Customize tab and pick a preset

## Build

The project is built by the GitHub Actions workflow in `.github/workflows/build.yml`. It downloads the fonts, signs the release build and uploads a universal APK as an artifact. Push a tag starting with `v` to publish it as a release.

To use a fixed signing key, add these repository secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD`.

To build locally with Gradle 8.14 and JDK 17, place `nunito.ttf`, `quicksand.ttf` and `fredoka.ttf` in `app/src/main/res/font` and run `gradle assembleRelease`.

## Status

Tested on Android 16 with Evolution X. Other versions and launchers may need adjustments, the Debug log option in Settings helps with reports.

## Credits

- [libxposed](https://github.com/libxposed) and [LSPosed](https://github.com/LSPosed/LSPosed)
- Fonts: Nunito, Quicksand and Fredoka, licensed under the SIL Open Font License

## License

GPL-3.0. See the LICENSE file.
