package com.spearline.utils

import android.content.Context
import android.os.Build
import com.spearline.watchrtc.logger.WatchRTCLoggerImpl
import com.spearline.watchrtc.sdk.EventType
import com.spearline.watchrtc.sdk.RtcDataProvider
import com.spearline.watchrtc.sdk.WatchRTC
import com.spearline.watchrtc.sdk.WatchRTCConfig
import com.spearline.webrtc.BuildConfig
import java.util.*
import java.util.concurrent.TimeUnit


object WatchRTCUtils {
    private lateinit var watchRTC: WatchRTC
    fun init(dataProvider: RtcDataProvider) {
        watchRTC = WatchRTC(dataProvider)
        watchRTC.setLoggerImpl(WatchRTCLoggerImpl())
    }

    fun connect(context: Context, roomId: String) {
        val config = WatchRTCConfig(
            BuildConfig.api_key,
            roomId,
            getPeerId(),
            HashMap<String, ArrayList<String>>().apply {
                put("company", ArrayList<String>().apply { add("Spearline") })
            }
        )
        watchRTC.setConfig(config)
        watchRTC.connect(context)
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
        try {
            watchRTC.trace(event, data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Generating unique PeerId for testing purpose, You can change as per requirement.
    private fun getPeerId(): String {
        var timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
        if (timeStamp.length >= 4) {
            timeStamp = timeStamp.takeLast(4)
        }
        return "PC_" + getRandomNumber() + "_" + Build.MANUFACTURER + "_" + timeStamp
    }

    private fun getRandomNumber(): Int {
        val rand = Random()
        val maxNumber = 100
        return rand.nextInt(maxNumber) + 1
    }


}