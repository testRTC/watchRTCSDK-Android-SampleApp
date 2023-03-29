
# WatchRTC Android sample app

A simple native WebRTC demo using watchRTC android sdk. Here is the [link] for watchRTC sdk.

## Disclaimer
This demo app's purpose is to demonstrate the bare minimum required to establish peer to peer connection with WebRTC to use of watchRTC android sdk. This is not a production ready code! In order to have a production VoIP app you will need to have a real signaling server deploy your own Turn server(s) and  integrate it.

## Requirements
- Latest android studio Like Electric Eel and above
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


![Screenshot from 2022-09-01 19-06-46](https://user-images.githubusercontent.com/77330472/187927818-cfcabb91-15a6-4b84-99e0-1e53c228b463.png)

[link]: https://github.com/testRTC/watchRTCSDK-Android

