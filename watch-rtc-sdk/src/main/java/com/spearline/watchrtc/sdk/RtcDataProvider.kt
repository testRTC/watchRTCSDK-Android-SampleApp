package com.spearline.watchrtc.sdk


interface RtcDataProvider {
    /**
     * Used by WatchRTC to periodically fetch statistics of the current peer connection.
     * Implementation of this interface should collect stats and once available, call callback.onStatsAvailable(stats).
     * Output is expected to be a JSON string, structured as
     * an RTCStatsReport object.
     * [docs](https://developer.mozilla.org/en-US/docs/Web/API/RTCStatsReport)
     * */
    fun getStats(callback: GetStatsCallback)
}