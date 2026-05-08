package com.rafa.musicas.online

object OnlineFileNameTemplate {

    fun apply(
        template: String,
        video: OnlineVideo,
        extension: String,
        number: String? = null
    ): String {
        val raw = template
            .replace("\$numc", number.orEmpty())
            .replace("\$num", number?.let { "[$it]" }.orEmpty())
            .replace("\$id", video.videoId)
            .replace("\$title", video.title)
            .replace("\$author", video.channelTitle)
            .trim()
            .ifBlank { video.title }
            .plus(".")
            .plus(extension.trimStart('.'))

        return sanitizeFileName(raw)
    }

    private fun sanitizeFileName(value: String): String {
        val invalidChars = charArrayOf(
            '\\', '/', ':', '*', '?', '"', '<', '>', '|'
        )

        return value
            .map { char ->
                if (char in invalidChars || char.code < 32) '_' else char
            }
            .joinToString("")
            .replace(Regex("_+"), "_")
            .trim()
            .take(180)
            .ifBlank { "arquivo" }
    }
}
