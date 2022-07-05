package com.spearline.watchrtc.network.websocket

import android.net.Uri
import com.spearline.watchrtc.BuildConfig
import com.spearline.watchrtc.sdk.WatchRTC
import com.spearline.watchrtc.sdk.WatchRTCConfig
import com.spearline.watchrtc.compression.LZString
import com.spearline.watchrtc.network.Constants
import com.spearline.watchrtc.network.Constants.API_KEY_QUERY_PARAM
import com.spearline.watchrtc.network.Constants.TIMESTAMP_QUERY_PARAM
import com.spearline.watchrtc.network.WatchRTCWebSocketListener
import com.spearline.watchrtc.util.JsonUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.logging.HttpLoggingInterceptor
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*

internal object WebSocketManager {

    private lateinit var client: OkHttpClient
    private val httpLogger: HttpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private var socket: WebSocket? = null
    private val gson = JsonUtil.getGsonInstance()

    private var messagesBuffer = ArrayList<ArrayList<Any?>>()

    private const val CLOSE_CODE_NORMAL = 1000
    private const val CLOSE_REASON_NORMAL = "bye"

    private const val STAGING_URL = "wss://watchrtc-staging2.testrtc.com/"
    private const val PRODUCTION_URL = "wss://watchrtc.testrtc.com/"

    fun connect(config: WatchRTCConfig, listener: WatchRTCWebSocketListener): WebSocket? {
        val httpClientBuilder = OkHttpClient.Builder().addInterceptor(httpLogger)
        var baseUrl = wsUrl(config.rtcApiKey)
        if (config.proxyUrl != null) {
            baseUrl = config.proxyUrl!!
        }
        client = httpClientBuilder.build()
        val request = Request.Builder().url(baseUrl).build()
        socket = client.newWebSocket(request, listener)
        return socket
    }

    fun close() {
        socket?.close(CLOSE_CODE_NORMAL, CLOSE_REASON_NORMAL)
    }

    private fun wsUrl(apiKey: String): String {
        var baseUrl = PRODUCTION_URL
        if (apiKey.startsWith("staging:")) {
            baseUrl = STAGING_URL
        }
        val timestamp = Date().time
        val encodedApiKey = java.net.URLEncoder.encode(apiKey.replace("staging:", ""), "utf-8")
        return "$baseUrl?$API_KEY_QUERY_PARAM=$encodedApiKey&$TIMESTAMP_QUERY_PARAM=$timestamp"
    }

    fun trace(eventName: String, data: Any?): Boolean {
        val message = ArrayList<Any?>().apply {
            add(eventName)
            add(Constants.DEFAULT_PEER_ID)
            add(data)
            add(Date().time)
        }

        messagesBuffer.add(message)

        if (socket == null) {
            return false
        }


        var sent = false
        val sendInterval = WatchRTC.serverConfig?.getInt(Constants.SEND_INTERVAL_CONFIG_KEY) ?: 1
        if (messagesBuffer.size >= sendInterval) {
            val messagesJson = gson.toJson(messagesBuffer)
            val compressedData = LZString.compressToEncodedURIComponent(messagesJson)
            sent = socket!!.send(compressedData)
            messagesBuffer.clear()
        }

        return sent
    }


}