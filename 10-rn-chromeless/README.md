# THEOplayer React Native Reference App

THEO Technologies does not provide THEOplayer React Native components. These apps describes how our current THEOplayer iOS and Android SDKs can be wrapped in React Native Bridges. The sample React Native bridge code is provided AS-IS without any explicit nor implicit guarantees. The React Native bridge sample code only provides mapping for a number of commonly used THEOplayer APIs, it is the customer’s responsibility to further expand the mapping and subsequently maintain the code and ensure compatibility with future versions of THEOplayer SDKs.

This is the Chromeless basic application in React native with theoplayer. This app decribes on how to create a chromeless project of THEOplayer and React Native in Android, iOS and tvOS.


## Getting Started on Android:
### Usage
##### Run project:
- install app libs `yarn`,
- run project `yarn run start`.

##### Run emulator:
- open project folder `<path to project>/android`,
- add emulator device configuration(AVD Manager).

##### Add THEOplayer SDK library:

The project has been configured to load the THEOplayer Android SDK through Maven.
You must enter your license string at `android/app/src/main/java/com/theoplayerreactnative/TheoPlayerViewManager.java` instead of `your_license_string` to configure the Android SDK for your license.
More information is available at https://github.com/THEOplayer/theoplayer-sdk-android.

Alternatively, instead of using Maven, you can manually include your THEOplayer Android SDK.
Refer to the getting started guide at https://docs.theoplayer.com/getting-started/01-sdks/02-android/00-getting-started.md for more information on this approach.

Note: Please use minapi16-THEOplayer SDK for Android devices starting from 4.1 and above, while minapi21-THEOplayer SDK can be used for Android 5.0 and above.

## Getting Started on iOS:

### Import THEOplayer SDK:
- Drag and drop THEOplayer iOS SDK Framework file respectively in [ios/THEOlibs](./ios/THEOlibs) folder for iOS and tvOS.
- Add THEOplayer SDK Framework to the project as stated in our documentation: [Getting Started with tvOS and iOS in React Native](https://docs.portal.theoplayer.com/getting-started/02-frameworks/03-react-native/02-add-tvos.md)

##### Install libs/depandencies:
- install app libs `yarn`,
- install pods `pod install`.

##### Run project and emulator:
- Open TheoPlayerReactNative.xcworkspace from the ios folder or open xcode, add new project -> select workspace file in ios folder generated after pod install,
- click run project, project will start automatically -> terminal & emulator will start

## License
This project is licensed under the BSD 3 Clause License - see the [LICENSE ](../LICENSE) file for details
