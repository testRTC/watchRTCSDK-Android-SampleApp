

<p align="center">
  <img height="160" src="https://avatars.githubusercontent.com/u/16746133?s=200&v=4" />
</p>

# watchrtc-android-sdk-demo

A simple native WebRTC demo using watchRTC android sdk. Here is the [link] for watchRTC sdk.

## Disclaimer
This demo app's purpose is to demonstrate the bare minimum required to establish peer to peer connection with WebRTC to use of watchRTC android sdk. This is not a production ready code! In order to have a production VoIP app you will need to have a real signaling server deploy your own Turn server(s) and  integrate it.

## Requirements
- android studio chipmunk 2021.2.1
- Android version 'Android-8' or later
- Need to add Firebase project json file to get access of firestore database and run the sample application.

## Run instructions
- Run application in two device.
- Enter the same meeting id in both device. i.e. `Test-Spearline:805`. The meeting id will append by random number so please enter the same number in second device to join the same meeting.
- Press `Start-Meeting` in one device once the call have been started then press `Join Meeting` on second device.
- Now the call is active mode you can disconnect the call and check on watchrtc portal for call quality and other information.
- To restart the process, kill both apps from the devices and repeat the above steps.

![Screenshot from 2022-09-01 19-06-46](https://user-images.githubusercontent.com/77330472/187927818-cfcabb91-15a6-4b84-99e0-1e53c228b463.png)

## Note
- If you are using the this sample app then you have to paste your api-key in the `watchrtc-demo` module build.gradle.

[link]: https://github.com/testRTC/watchRTCSDK-Android

