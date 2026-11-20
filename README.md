# LaunchAnimCustomizer

An LSPosed module that lets you tune the spring physics of the launcher open and close animations on Pixel and AOSP based ROMs. Changes apply live, without restarting the launcher.

## Features

- Simple tuning: one speed slider and a wallpaper blur slider, applied on top of the system animation or any theme
- Advanced tuning for opening: real duration, separate curves for main, horizontal and corner motion, corner roundness, shadow, icon fade timing and start point
- Advanced tuning for closing: springs with stiffness and damping, optionally separate for horizontal, vertical and scale, plus corner roundness
- Curves: spring, overshoot, anticipate, bounce, decelerate, accelerate, smooth step, elastic, stepped, hold-then-go, ease, custom Bezier and linear
- Thirty-six ready made themes across six categories (system style, fast, springy, cinematic, playful, shape), plus your own saved themes
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

- Opening from an icon: the launcher drives the window with one animator and a set of per property interpolators. The module swaps the interpolators, sets the animator duration, and adjusts corner radius, shadow, icon fade and start point on the running listener.
- Closing and return to home: `RectFSpringAnim` reads its spring values through `DynamicResource`. The module replaces the stiffness and damping values while a closing animation starts scales the corner radii of the close runner, and scales the wallpaper blur returned by the depth controller.

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
