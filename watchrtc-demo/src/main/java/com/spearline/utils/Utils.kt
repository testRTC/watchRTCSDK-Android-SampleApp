package com.spearline.utils

import android.content.Context
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

object Utils {

    fun writeFile(stringData: String, context: Context) {
        val file =
            File(context.cacheDir, "WebRTCStatsReport_${System.currentTimeMillis()}.txt")
        Log.d("Utils", "File Path: ${file.absoluteFile}")
        Log.d("Utils", "File exist: ${file.exists()}")
        val fw = FileWriter(file.absoluteFile)
        val bw = BufferedWriter(fw)
        bw.write(stringData)
        bw.close()
    }

}