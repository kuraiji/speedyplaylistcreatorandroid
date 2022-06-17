package com.kuraiji.speedyplaylistcreator.data

import android.net.Uri
import androidx.room.*
import android.media.MediaMetadataRetriever

object PlaylistManager {
    fun indexTracks(uris: ArrayList<Uri>) {
        uris.forEach {uri ->
            val metadata = MediaMetadataRetriever()
            metadata.setDataSource(uri.path)
            //Room.databaseBuilder()
        }
    }
}

private class PlaylistData {
    @Entity
    data class Track(
        @PrimaryKey val id: Long,
        val title: String,
        val trackNum: Int,
        val discNum: Int,
        val fileLocation: String,
        val trackAlbumId: Long,
        val trackArtistId: Long
    )

    @Entity
    data class Album(
        @PrimaryKey val albumId: Long,
        val title: String
    )

    @Entity
    data class Artist(
        @PrimaryKey val artistId: Long,
        val name: String
    )

    data class AlbumWithTracks(
        @Embedded val album: Album,
        @Relation(
            parentColumn = "albumId",
            entityColumn = "trackAlbumId"
        )
        val tracks: List<Track>
    )

    data class ArtistWithTracks(
        @Embedded val artist: Artist,
        @Relation(
            parentColumn = "artistId",
            entityColumn = "trackArtistId"
        )
        val tracks: List<Track>
    )

    @Dao
    interface AlbumDao {
        @Insert
        fun insert(vararg albums: Album)
    }

    @Dao
    interface ArtistDao {
        @Insert
        fun insert(vararg artists: Artist)
    }

    @Dao
    interface TrackDao {
        @Insert
        fun insert(vararg tracks: Track)
    }
}