package com.rafa.musicas.online

enum class OnlineQualityPreference {
    LOWEST,
    UP_TO_360P,
    UP_TO_480P,
    UP_TO_720P,
    UP_TO_1080P,
    HIGHEST
}

fun OnlineQualityPreference.displayName(): String {
    return when (this) {
        OnlineQualityPreference.LOWEST -> "Menor qualidade"
        OnlineQualityPreference.UP_TO_360P -> "Até 360p"
        OnlineQualityPreference.UP_TO_480P -> "Até 480p"
        OnlineQualityPreference.UP_TO_720P -> "Até 720p"
        OnlineQualityPreference.UP_TO_1080P -> "Até 1080p"
        OnlineQualityPreference.HIGHEST -> "Maior qualidade"
    }
}
