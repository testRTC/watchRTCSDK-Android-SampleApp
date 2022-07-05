package com.spearline.watchrtc.sdk

import com.google.gson.annotations.SerializedName

/**
 * @param rtcApiKey WatchRTC api key
 * @param rtcPeerId Peer connection id
 * @param rtcRoomId RTC room id
 * @param keys optional key-value pairs to be sent to WatchRTC's backend
 * */
class WatchRTCConfig(
    @SerializedName("rtcApiKey")
    val rtcApiKey: String,
    @SerializedName("rtcRoomId")
    val rtcRoomId: String,
    @SerializedName("rtcPeerId")
    val rtcPeerId: String,
    @SerializedName("keys")
    internal var keys: HashMap<String, ArrayList<String>>? = null,
    var proxyUrl: String? = null
) {

    internal fun isValid(): Boolean {
        if (rtcApiKey.isEmpty() || rtcRoomId.isEmpty() || rtcPeerId.isEmpty()) {
            return false
        }

        return true
    }

    internal fun addKeys(keys: HashMap<String, ArrayList<String>>) {
        if (this.keys == null) {
            this.keys = HashMap()
        }
        this.keys!!.putAll(keys)
    }
}
