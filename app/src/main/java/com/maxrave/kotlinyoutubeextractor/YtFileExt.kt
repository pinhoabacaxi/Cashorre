package com.maxrave.kotlinyoutubeextractor

import android.util.SparseArray

fun SparseArray<YtFile>.getAudioOnly(): ArrayList<YtFile> {
    val resultList = ArrayList<YtFile>()
    val audioItags = listOf(140, 171, 249, 250, 251)
    for (itag in audioItags) {
        this[itag]?.let { resultList.add(it) }
    }
    return resultList
}

fun ArrayList<YtFile>.bestQuality(): YtFile? {
    if (this.isEmpty()) return null
    return this.maxByOrNull { it.meta?.audioBitrate ?: 0 }
}
