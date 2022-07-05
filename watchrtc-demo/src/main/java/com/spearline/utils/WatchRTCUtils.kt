package com.spearline.utils

import com.spearline.watchrtc.sdk.EventType
import com.spearline.watchrtc.sdk.RtcDataProvider
import com.spearline.watchrtc.sdk.WatchRTC
import com.spearline.watchrtc.sdk.WatchRTCConfig


object WatchRTCUtils {
    private lateinit var watchRTC: WatchRTC
    fun init(dataProvider: RtcDataProvider) {
        watchRTC = WatchRTC(dataProvider)
    }

    fun connect(roomId: String) {
        val config = WatchRTCConfig(
            "staging:6d3873f0-f06e-4aea-9a25-1a959ab988cc",
            roomId,
            "PC_0",
            HashMap<String, ArrayList<String>>().apply {
                put("company", ArrayList<String>().apply { add("Moveo") })
            }
        )
        watchRTC.setConfig(config)
        watchRTC.connect()
        watchRTC.addEvent(
            "custom_test_event",
            EventType.Global,
            HashMap<String, String>().apply { put("test", "data") })
    }

    fun disconnect() {
        try {
            watchRTC.disconnect()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    fun logEvents(event: String, data: Any?) {
        watchRTC.trace(event, data)
    }

}