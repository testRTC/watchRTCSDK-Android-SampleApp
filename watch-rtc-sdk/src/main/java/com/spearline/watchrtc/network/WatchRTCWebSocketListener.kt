package com.spearline.watchrtc.network

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONException
import org.json.JSONObject

internal abstract class WatchRTCWebSocketListener : WebSocketListener() {

    companion object {
        const val TAG = "WatchRTCWebSocketListener"
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.i(TAG, "WebSocket::onOpen")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.i(TAG, "WebSocket::onClosed")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        try {
            val message = JSONObject(text)
            val errorReason = handleError(message)
            if (errorReason.isNullOrBlank()) {
                if (message.has(Constants.PROJECT_ID_CONFIG_KEY)) {
                    onConfigAvailable(message)
                }
            } else {
                Log.e(TAG, "WebSocket::onMessage - error from server: [$errorReason]")
            }

        } catch (e: JSONException) {
            Log.e(TAG, "WebSocket::onMessage - failed to parse message: [$text]", e)
        } finally {
            Log.i(TAG, "message received from server: $text")
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.e(TAG, "WebSocket::onFailure", t)
    }

    private fun handleError(message: JSONObject): String? {
        var reason: String? = null
        if (message.has("error")) {
            reason = message.getString("error")
        }
        return reason
    }

    abstract fun onConfigAvailable(config: JSONObject)


}