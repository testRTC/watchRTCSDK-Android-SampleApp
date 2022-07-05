package com.spearline.watchrtc.scheduler

import com.spearline.watchrtc.compression.DeltaCompression
import com.spearline.watchrtc.model.RTCStatsReport
import com.spearline.watchrtc.network.Constants
import com.spearline.watchrtc.network.Constants.INTERVAL_CONFIG_KEY
import com.spearline.watchrtc.network.websocket.WebSocketManager
import com.spearline.watchrtc.sdk.GetStatsCallback
import com.spearline.watchrtc.sdk.RtcDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

internal object GetStatsScheduler {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var job: Job
    private var isRunning = false
    private var prevReport: RTCStatsReport? = null

    private suspend fun statsCoroutine(dataProvider: RtcDataProvider): RTCStatsReport {
        return suspendCoroutine { continuation ->
            dataProvider.getStats(object : GetStatsCallback {
                override fun onStatsAvailable(stats: RTCStatsReport) {
                    continuation.resume(stats)
                }
            })
        }
    }

    @OptIn(ExperimentalTime::class)
    fun start(
        config: JSONObject,
        dataProvider: RtcDataProvider
    ) {
        val intervalInSeconds = config.getInt(INTERVAL_CONFIG_KEY)
        val getStatsFlow = tickerFlow(milliseconds(intervalInSeconds))

        job = getStatsFlow.map { statsCoroutine(dataProvider) }
            .onEach {
                val statsToSend: RTCStatsReport? = if (prevReport != null) {
                    DeltaCompression.diff(prevReport, it)
                } else {
                    it
                }
                prevReport = it
                if (statsToSend != null) {
                    WebSocketManager.trace(
                        Constants.WEB_SOCKET_MESSAGE_GET_STATS,
                        statsToSend.toData()["report"]
                    )
                }
            }
            .launchIn(scope)
        isRunning = true
    }

    fun stop() {
        if (isRunning) {
            job.cancel()
            isRunning = false
        }
    }

    @OptIn(ExperimentalTime::class)
    fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
        delay(initialDelay)
        while (true) {
            emit(Unit)
            delay(period)
        }
    }
}
