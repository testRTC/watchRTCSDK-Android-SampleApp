
# WatchRTC Android sample app

A simple native WebRTC demo using watchRTC android sdk. Here is the [link] for watchRTC sdk.

## Requirements
- Latest android studio i.e. Electric Eel and above
- Android version 'Android-7' or later
- Need to add Firebase project json file, To get access of firestore database and run the sample application.

## Run instructions
- Add `api_key` in `watchrtc-demo` module build.gradle file.
- Run application in two device.
- Enter the same meeting id in both device. i.e. `Test-Spearline:805`. The meeting id will append by random number so please enter the same number in second device to join the same meeting.
- Press `Start-Meeting` in one device once the call have been started then press `Join Meeting` on second device.
- Now the call is active mode you can disconnect the call and check on watchrtc portal for call quality and other information.
- To restart the process, kill both apps from the devices and repeat the above steps.
- Once the call finished you can visit WatchRTC portal and check call collection stats information.

## Disclaimer
This sample app's purpose is to demonstrate the bare minimum required to establish peer to peer connection with WebRTC & main use to elaborate android WatchRTC SDK in application. 
This is not a production ready code. 

<p align="center">
  <img height="512" src="https://user-images.githubusercontent.com/77330472/228528826-f28ef847-610b-4193-b2c0-5b2a5fb3555b.jpg" />
</p>

[link]: https://github.com/testRTC/watchRTCSDK-Android


