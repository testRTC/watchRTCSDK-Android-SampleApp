package com.spearline.watchrtc.sdk

import com.spearline.watchrtc.network.Constants
import com.spearline.watchrtc.network.WatchRTCWebSocketListener
import com.spearline.watchrtc.network.websocket.WebSocketManager
import com.spearline.watchrtc.scheduler.GetStatsScheduler
import org.json.JSONObject

/**
 * @param config @see WatchRTCConfig
 * @param dataProvider implementation of RTC stats data collection
 * */
class WatchRTC(
    private var config: WatchRTCConfig?,
    private val dataProvider: RtcDataProvider
) {

    internal companion object {
        var serverConfig: JSONObject? = null
    }

    var isConnected = false

    /**
     * Note: setConfig must be called before connect in case
     * config is not passed in the constructor
     * @param dataProvider
     * */
    constructor(dataProvider: RtcDataProvider) : this(
        null,
        dataProvider
    )

    /**
     * Initialize connection to WatchRTC's backend.
     * Should be called once peer connection is open
     * @throws IllegalStateException in case config is not set via
     * constructor or @see setConfig
     * @throws IllegalArgumentException in case one of the config values is invalid
     * */
    fun connect(config: WatchRTCConfig? = null) {
        if (isConnected) {
            return
        }
        var configToUse = config
        if (configToUse == null) {
            configToUse = this.config
        }
        if (configToUse == null) {
            throw IllegalStateException("connect called before configuration is set")
        }

        if (!configToUse.isValid()) {
            throw IllegalArgumentException("invalid provided config. make sure rtcApiKey, rtcRoomId and rtcPeerId are set and valid")
        }

        WebSocketManager.connect(configToUse, object : WatchRTCWebSocketListener() {
            override fun onConfigAvailable(config: JSONObject) {
                isConnected = true
                serverConfig = config
                sendCreateMessage()
                sendWatchRtcMessage()
                sendMetadataMessage()
                GetStatsScheduler.start(config, dataProvider)
            }
        })
    }

    /**
     * Will be sent to WatchRTC's backend.
     * @param keys Custom key-value pairs.
     * */
    fun addKeys(keys: HashMap<String, ArrayList<String>>) {
        this.config?.addKeys(keys)
    }

    /**
     * Set WatchRTC configuration
     * @param config WatchRTC connection configuration
     * @see WatchRTCConfig
     * */
    fun setConfig(config: WatchRTCConfig) {
        this.config = config
    }

    /**
     * Closes connection to WatchRTC's backend.
     * Should be called once the peer connection is closed.
     * */
    fun disconnect() {
        if (isConnected) {
            WebSocketManager.close()
            GetStatsScheduler.stop()
            isConnected = false
        }
    }

    /**
     * Set user provided rating with an optional comment.
     * @param rating A number from 1 to 5. You can use it for a 5-stars rating system,
     * or you can use 1 and 5 values only for a like/dislike type of a rating system
     * @param ratingComment Simple string value, collecting user's "verbal" feedback
     * */
    fun setUserRating(rating: Int, ratingComment: String = "") {
        val data = HashMap<String, Any>().apply {
            put("rating", rating)
            put("ratingComment", ratingComment)
        }
        WebSocketManager.trace(Constants.WEB_SOCKET_MESSAGE_USER_RATING, data)
    }

    /**
     * Log debug messages to WatchRTC's server
     * @param logLevel
     * @param text log message
     * */
    fun log(logLevel: LogLevel, text: String) {
        val data = HashMap<String, String>().apply {
            put("type", logLevel.name.lowercase())
            put("text", text)
        }
        WebSocketManager.trace(Constants.WEB_SOCKET_MESSAGE_LOG, data)
    }

    /**
     * Send RTC related events to WatchRTC's backend.
     * @param eventName event name
     * @param data Event data.
     * */
    fun trace(eventName: String, data: Any?): Boolean {
        return WebSocketManager.trace(eventName, data)
    }

    /**
     * Send custom events to WatchRTC's backend.
     * @param name custom event name
     * @param type Event type. @see EventType.
     * @param parameters Optional custom data.
     * */
    fun addEvent(name: String, type: EventType, parameters: Any? = null) {
        val data = HashMap<String, Any?>().apply {
            put("name", name)
            put("type", type.toString())
            put("parameters", parameters)
        }
        WebSocketManager.trace(Constants.WEB_SOCKET_MESSAGE_CUSTOM_EVENT, data)
    }


    //region private

    private fun sendWatchRtcMessage() {
        WebSocketManager.trace(Constants.WEB_SOCKET_MESSAGE_WATCH_RTC, this.config)
    }

    private fun sendCreateMessage() {
        WebSocketManager.trace(Constants.WEB_SOCKET_MESSAGE_CREATE, null)
    }

    private fun sendMetadataMessage() {
        val metadata = HashMap<String, Any>().apply {
            put("browser", "native")
            put("os", "Android")
        }
        WebSocketManager.trace(Constants.WEB_SOCKET_MESSAGE_METADATA, metadata)
    }


    /**
     * {
    "gpu": "google, vulkan 1.2.0 (swiftshader device (subzero) (0x0000c0de)), swiftshader driver) (ANGLE (Google, Vulkan 1.2.0 (SwiftShader Device (Subzero) (0x0000C0DE)), SwiftShader driver))",
    "isMobile": false,
    "tier": 1,
    "type": "FALLBACK",
    "cores_count": 8,
    "jsHeapSizeLimit": "2072.00",
    "totalJSHeapSize": "6.96",
    "usedJSHeapSize": "7.03"
    }
     * */
    private fun sendHardwareMessage() {

        val runtime = Runtime.getRuntime()

        val hardwareInfo = HashMap<String, Any>().apply {
            put("cores_count", runtime.availableProcessors())
            put("os", "Android")
        }

        WebSocketManager.trace(Constants.WEB_SOCKET_MESSAGE_HARDWARE, hardwareInfo)
    }

    //endregion

}

