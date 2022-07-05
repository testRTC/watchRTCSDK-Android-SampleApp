package com.spearline.watchrtc.network

internal object Constants {
    const val API_KEY_QUERY_PARAM = "apiKey"
    const val TIMESTAMP_QUERY_PARAM = "timestamp"

    const val WEB_SOCKET_MESSAGE_CREATE = "create"
    const val WEB_SOCKET_MESSAGE_METADATA = "metadata"
    const val WEB_SOCKET_MESSAGE_GET_STATS = "getstats"
    const val WEB_SOCKET_MESSAGE_WATCH_RTC = "watchrtc"
    const val WEB_SOCKET_MESSAGE_LOG = "log"
    const val WEB_SOCKET_MESSAGE_HARDWARE = "hardware"
    const val WEB_SOCKET_MESSAGE_USER_RATING = "userRating"
    const val WEB_SOCKET_MESSAGE_CUSTOM_EVENT = "event"

    const val PROJECT_ID_CONFIG_KEY = "projectId"
    const val INTERVAL_CONFIG_KEY = "interval"
    const val SEND_INTERVAL_CONFIG_KEY = "sendInterval"
    const val DEFAULT_PEER_ID = "PC_0"
}