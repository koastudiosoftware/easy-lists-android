<div align="center">

<!-- <p><img src="https://github.com/koastudiosoftware/easy-lists-android/blob/main/metadata/images/icon.png" width="200"></p> -->

# Easy Lists

### Lightweight List Manager

[![Android](https://img.shields.io/badge/Android-grey?logo=android&style=flat)](https://www.android.com/)
[![AndroidAPI](https://img.shields.io/badge/API-37%2B-859900.svg?style=flat)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-6c71c4.svg?logo=kotlin)](https://kotlinlang.org)
[![JetpackCompose](https://img.shields.io/badge/Jetpack%20Compose-1.8.0-b58900)](https://developer.android.com/jetpack/compose)
[![Release](https://badgen.net/github/release/koastudiosoftware/easy-lists-android?color=dc322f)](https://github.com/koastudiosoftware/easy-lists-android/releases)

</div>

 ----

Lightweight and fast list manager for Android


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
Easy Lists stores your lists locally and updates them automatically whenever you've changed them. Local backups can be created at any time. Our app can be used virtually off-line, if desired. 

Cloud sync is offered for those who choose to sign in with their Google account. Sync and sharing is offered for subscribers. Sync automatically stores your lists, list items, categories, tags, and photos in our secure cloud-based database.

Multi-device support already exists. Simply create some lists, sign into your Google account, enable the sync option in the app settings, and then sign into the app with the same Google account on a second device, enable sync on that device, and wait for the sync to happen.

### Designed for Android
The User Interface has been designed by following the latest Google's Material Design guidelines and by using only native Android components and animations.

As of Android API 31, dynamic color theming is supported. Easy Lists supports dynamic color though the interface doesn't really see much of what dynamic color theming has to offer.

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

- [olshevski](https://github.com/olshevski): for the [Compose Navigation Reimagined](https://github.com/olshevski/compose-navigation-reimagined) library

- [max-contrast](https://github.com/Myndex/max-contrast): for calculating the text color to display over custom tag colors

- [colorpicker-compose](https://github.com/skydoves/colorpicker-compose): for tag color wheel/slider selection

- [firebase](insert link here)

- [googleid](insert link here)

## Support




