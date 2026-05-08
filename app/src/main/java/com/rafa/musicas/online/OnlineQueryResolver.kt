package com.rafa.musicas.online

class OnlineQueryResolver(
    private val repository: OnlineRepository
) {
    suspend fun resolve(query: String): OnlineQueryResult {
        val cleanQuery = query.trim()

        if (cleanQuery.isBlank()) {
            return OnlineQueryResult(
                kind = OnlineQueryResultKind.SEARCH,
                title = "Busca vazia",
                videos = emptyList()
            )
        }

        val forcedSearch =
            if (cleanQuery.startsWith("?")) {
                cleanQuery.drop(1).trim()
            } else {
                cleanQuery
            }

        val videos = repository.search(forcedSearch)

        return OnlineQueryResult(
            kind = OnlineQueryResultKind.SEARCH,
            title = "Busca: $forcedSearch",
            videos = videos
        )
    }
}
