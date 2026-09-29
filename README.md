<div align="center">

<!-- <p><img src="https://github.com/CoinTrend/CoinTrend/blob/develop/metadata/en-US/images/icon.png" width="200"></p> -->

# Easy Lists

### Lightweight List Manager

[![Android](https://img.shields.io/badge/Android-grey?logo=android&style=flat)](https://www.android.com/)
[![AndroidAPI](https://img.shields.io/badge/API-37%2B-859900.svg?style=flat)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-6c71c4.svg?logo=kotlin)](https://kotlinlang.org)
[![JetpackCompose](https://img.shields.io/badge/Jetpack%20Compose-1.8.0-b58900)](https://developer.android.com/jetpack/compose)
[![Release](https://badgen.net/github/release/koastudiosoftware/easy-lists-android?color=dc322f)](https://github.com/koastudiosoftware/easy-lists-android/releases)

[![Resultat](https://img.shields.io/badge/Resultat-1.0.0-d33682)](https://github.com/nicolashaan/resultat)

</div>

 ----

Lightweight, fast and private list manager for Android


## Features

- **Lists**: create personalized lists where you can keep track of items (e.g. grocery lists, packing lists, future purchases, ...)
- **Share**: share your lists with others
- **Settings**: customize your in-app experience

<p float="left">
  <img src="https://github.com/koastudiosoftware/easy-lists-android/blob/main/metadata/images/phone-001.png" width="19%" />
  <img src="https://github.com/koastudiosoftware/easy-lists-android/blob/main/metadata/images/phone-002.png" width="19%" />
  <img src="https://github.com/koastudiosoftware/easy-lists-android/blob/main/metadata/images/phone-003.png" width="19%" />
  <img src="https://github.com/koastudiosoftware/easy-lists-android/blob/main/metadata/images/phone-004.png" width="19%" />
  <img src="https://github.com/koastudiosoftware/easy-lists-android/blob/main/metadata/images/phone-005.png" width="19%" />
</p>

### Lightweight
Easy Lists stores your lists locally and updates them automatically whenever you've changed them.

### Designed for Android
The User Interface has been designed by following the latest Google's Material Design guidelines and by using only native Android components and animations.

As of Android API 31, dynamic color theming is supported. However, as this project is designed to offer a highly readable theme based on the Solarized color palette created by Ethan Schoonover, we have disabled dynamic color. Dynamic color completely adjusts the theme colors automatically and the result bears no resemblance to the Solarized color palette.

## Technical Details

- **100% Jetpack Compose** 🚀

- **Material Design 3** 💎

- **Multimodule Clean Architecture** 🏛 as [davidepanidev](https://github.com/davidepanidev)'s [Clean Architecture Compose Concept](https://github.com/davidepanidev/android-multimodule-architecture-concepts/tree/clean-architecture-compose-concept) which consists of four separate modules:
  -  _app_: Android module that contains the Android Application component and all the framework specific configurations. It has visibility over all the other modules and defines the global dependency injection configurations.
  -  _presentation_: Android **MVVM**-based module. It contains the Android UI framework components (Activities, Composables, ViewModels...) and the related resources (e.g. images, strings...). This module just observes data coming from the undelying modules through Kotlin Flows and displays it. 
  -  _domain_: Kotlin module that contains platform-independent business logic, the entities (platform-independent business models), and the repository interfaces.
  -  _data_: Android module that acts as the **Single-Source-Of-Truth** for the app's data gathering. It contains repositories implementations, the Room entities for persistence, the data source API implementations and the corresponding API-specific models.


## Powered By

[<a href="https://firebase.google.com/" target="_blank">Firebase</a>](https://firebase.google.com/)


## Credits

### Contributors

- [Dave Wiard](https://github.com/davewiard)

### Libraries and References

- [nicolashaan](https://github.com/nicolashaan): for the [Resultat](https://github.com/nicolashaan/resultat) library

- [olshevski](https://github.com/olshevski): for the [Compose Navigation Reimagined](https://github.com/olshevski/compose-navigation-reimagined) library

- [Solarized](https://ethanschoonover.com/solarized/): for the light and dark themes

- [max-contrast](https://github.com/Myndex/max-contrast): for calculating the text color to display over custom tag colors

- [colorpicker-compose](https://github.com/skydoves/colorpicker-compose): for tag color wheel/slider selection

## Support

Easy Lists does not generate any revenue. If you wish to support the developers you can donate some sats at the Bitcoin address below:


