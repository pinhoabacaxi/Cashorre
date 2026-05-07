package com.rafa.musicas.data

import com.rafa.musicas.data.db.MusicEntity

data class ArtistGroup(
    val artist: String,
    val tracks: List<MusicEntity>
)

data class AlbumGroup(
    val album: String,
    val artworkUri: String?,
    val tracks: List<MusicEntity>
)

fun groupTracksByArtist(
    tracks: List<MusicEntity>
): List<ArtistGroup> {
    return tracks
        .groupBy {
            it.author.ifBlank { "Desconhecido" }
        }
        .map { (artist, tracks) ->
            ArtistGroup(
                artist = artist,
                tracks = tracks.sortedBy { it.displayName.lowercase() }
            )
        }
        .sortedBy { it.artist.lowercase() }
}

fun groupTracksByAlbum(
    tracks: List<MusicEntity>
): List<AlbumGroup> {
    return tracks
        .groupBy {
            it.album?.ifBlank { "Sem álbum" } ?: "Sem álbum"
        }
        .map { (album, tracks) ->
            AlbumGroup(
                album = album,
                artworkUri = tracks.firstOrNull()?.artworkUri,
                tracks = tracks.sortedBy { it.displayName.lowercase() }
            )
        }
        .sortedBy { it.album.lowercase() }
}
