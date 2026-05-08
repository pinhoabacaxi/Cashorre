package com.rafa.musicas.online

enum class OnlineQueryResultKind {
    VIDEO,
    PLAYLIST,
    CHANNEL,
    SEARCH,
    AGGREGATE
}

data class OnlineQueryResult(
    val kind: OnlineQueryResultKind,
    val title: String,
    val videos: List<OnlineVideo>
) {
    companion object {
        fun aggregate(results: List<OnlineQueryResult>): OnlineQueryResult {
            require(results.isNotEmpty()) {
                "Cannot aggregate empty results."
            }

            val kind =
                if (results.size == 1) {
                    results.first().kind
                } else {
                    OnlineQueryResultKind.AGGREGATE
                }

            val title =
                if (results.size == 1) {
                    results.first().title
                } else {
                    "${results.size} consultas"
                }

            val videos = results
                .flatMap { it.videos }
                .distinctBy { it.videoId }

            return OnlineQueryResult(
                kind = kind,
                title = title,
                videos = videos
            )
        }
    }
}
