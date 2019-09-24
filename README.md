# DHAP

An Android library implementation of the [Decentralised Home Automation Protocol](https://decentralised-home-automation-protocol.github.io/DHAP-Documentation/)

This repo includes and example application which can perform all of the basic functionality needed to control DHAP compliant IoT devices. To use the example application, simply clone this repo and open it in Android Studio. From there you can compile and run the example app on your Android device.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Installation

Visit the [documentation website](https://decentralised-home-automation-protocol.github.io/DHAP-Documentation/guide/android.html#installation) for more detailed installation instructions.

### Requirements

- Android API 17+
- Android Studio 3.0+

### Gradle

**Step 1**: Add the JitPack repository to your root/project `build.gradle`

``` gradle {4}
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io/' }
  }
}
```

<br>

**Step 2**: Add the dependency by including it in your module's `app/build.gradle`

``` gradle {3}
dependencies {
  ...
  implementation 'com.github.decentralised-home-automation-protocol:DHAP-Android:0.2.0'
}
```

<br>

**Step 3**: Add Java 1.8 compatibility. Add the following compileOptions to your `app/build.gradle`

``` gradle {3-6}
android {
  ...
  compileOptions {
      targetCompatibility JavaVersion.VERSION_1_8
      sourceCompatibility JavaVersion.VERSION_1_8
  }
}
```

## Usage

Visit the [documentation website](https://decentralised-home-automation-protocol.github.io/DHAP-Documentation/guide/android.html) for usage instructions and API details.

## License

This project is licensed under the [MIT License](LICENSE)

## Credits

- **Tyler Steane** - Initial work
- **Aiden Garipoli** - Creator / Maintainer
- **Daniel Milner** - Creator / Maintainer
