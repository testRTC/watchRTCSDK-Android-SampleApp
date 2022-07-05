package com.spearline.watchrtc.compression

import com.spearline.watchrtc.model.RTCStatsReport

internal object DeltaCompression {

    fun diff(old: RTCStatsReport?, new: RTCStatsReport): RTCStatsReport? {
        if (old == null) {
            return new
        }

        val newCopy = RTCStatsReport.fromJson(new.toJson())

        var reportTimestamp = Long.MIN_VALUE

        val reportIterator = newCopy.report.iterator()

        // report loop
        while (reportIterator.hasNext()) {
            val newReportEntry = reportIterator.next()
            val oldReportEntryValue = old.report[newReportEntry.key]

            // old report contains current stat
            if (oldReportEntryValue != null) {
                // stat properties loop

                val propertiesIterator = newReportEntry.value.properties.iterator()
                while (propertiesIterator.hasNext()) {
                    val newPropertyEntry = propertiesIterator.next()

                    val oldPropertyEntryValue =
                        oldReportEntryValue.properties[newPropertyEntry.key]

                    if (oldPropertyEntryValue == newPropertyEntry.value) {

                        propertiesIterator.remove()
                    }
                }
            }

            if (newReportEntry.value.timestamp > reportTimestamp) {
                reportTimestamp = newReportEntry.value.timestamp
            }

            if (newReportEntry.value.properties.isEmpty()) {
                reportIterator.remove()
            }

            newCopy.timestamp = reportTimestamp
        }


        if (newCopy.report.isEmpty()) {
            return null
        }

        return newCopy
    }
}