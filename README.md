<p align="center">
  <img height="160" src="https://avatars.githubusercontent.com/u/16746133?s=200&v=4" />
</p>

# watchrtc-android-sdk

This repository contains the watchRTC android SDK.

The SDK is geared towards those who want to collect WebRTC related data from an android application, log and analyze it as part of the testRTC environment.

## SDK support and requirement
* The min android version supported by the SDK is Android-7 (Api Level 24).
* The SDK only required Internet permission.

## How to add SDK in your app
1. Download SDK from [here].
2. Paste SDK in your app module libs directory
3. Paste below code inside your app module build.gradle file.
    ```groovy
    implementation files('./src/main/libs/watch-rtc-sdk-release.aar')
    implementation 'com.squareup.okhttp3:okhttp:4.9.3'
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0'
    implementation 'com.google.code.gson:gson:2.9.0'
   ```
 4. Sync project
 
 ## How to implement SDK in your app
- Implement the `RtcDataProvider` interface and their method `getStats(callback: GetStatsCallback)` The method getStats jobs is to generate webrtc stats report and once the report is avaialble need to call `callback.onStatsAvailable()`
```Kotlin
  private val rtcDataProvider = object : RtcDataProvider {
        override fun getStats(callback: GetStatsCallback) {
            // get stats report and call callback.onStatsAvailable(com.spearline.watchrtc.model.RTCStatsReport)
        }
    }
```
- Initialize WatchRTCConfig with your API Key and room id and other details
```Kotlin
val config = WatchRTCConfig(
            "<api-key>",
            "<room-id>",
            "<peer-id>",
            "<keys>" //(optional)
        )
```
- Create WatchRTC object
```Kotlin
watchRTC = WatchRTC(rtcDataProvider, config)
```
- Connect to watchRTC's servers
```Kotlin
//Please call connect() once the peer connection is active
watchRTC.connect()
```

- Disconnect the call
```Kotlin
//Please call disconnect() once the call have been disconnected.
watchRTC.disconnect()
```
## You can also use some of the other functions of the WatchRTC SDK
- watchRTC.setConfig(WatchRTCConfig) //Set WatchRTC configuration
- watchRTC.addKeys(HashMap<String, ArrayList<String>>) //Will be sent to WatchRTC's backend.
- watchRTC.setUserRating(Int, String) //Set user provided rating with an optional comment.
- watchRTC.log(LogLevel,String) //Log debug messages to WatchRTC's server
- watchRTC.trace(String, Any?) : Boolean //Send RTC related events to WatchRTC's backend.
- addEvent(String, EventType, Any?) //Send custom events to WatchRTC's backend.


## If you are using the this sample app then you have to paste your api-key in the `watchrtc-demo` module.

[here]: https://github.com/testRTC/watchRTCSDK-Android-SampleApp/raw/master/watchrtc-demo/src/main/libs/watch-rtc-sdk-release.aar
