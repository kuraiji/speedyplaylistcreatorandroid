package com.kuraiji.speedyplaylistcreator.data

import android.content.Context
import android.net.Uri
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.kuraiji.speedyplaylistcreator.common.debugLog

object PlaylistManager {
    fun wipeDatabase(context: Context) {
        val db = Room.databaseBuilder(
            context,
            PlaylistData.PlaylistDatabase::class.java, "PlaylistDatabase"
        ).fallbackToDestructiveMigration().build()
        db.clearAllTables()
    }

    fun storeUris(uris: ArrayList<Uri>, context: Context) {
        val db = Room.databaseBuilder(
            context,
            PlaylistData.PlaylistDatabase::class.java, "PlaylistDatabase"
        ).fallbackToDestructiveMigration().build()
        db.clearAllTables()
        val uriDao = db.uriDao()
        uris.forEach { uri ->
            uriDao.insert(PlaylistData.Uri(0, uri.toString()))
        }
    }

    fun retrieveUris(uris: MutableLiveData<MutableList<Uri>>, context: Context) {
        debugLog("Retrieving...")
        val db = Room.databaseBuilder(
            context,
            PlaylistData.PlaylistDatabase::class.java, "PlaylistDatabase"
        ).fallbackToDestructiveMigration().build()
        val uriDao = db.uriDao()
        uriDao.selectAll().forEach { stringUri ->
            uris.value?.add(stringUri.uri.toUri())
        }
    }
}

private class PlaylistData {
    @Entity
    data class Track(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val title: String,
        val trackNum: Int,
        val discNum: Int,
        val trackUriId: Long,
        val trackAlbumId: Long,
        val trackArtistId: Long
    )

    @Entity
    data class Album(
        @PrimaryKey(autoGenerate = true)
        val albumId: Long,
        val title: String
    )

    @Entity
    data class Artist(
        @PrimaryKey(autoGenerate = true)
        val artistId: Long,
        val name: String
    )

    @Entity
    data class Uri(
        @PrimaryKey(autoGenerate = true)
        val uriId: Long,
        val uri: String
    )

    @Database(entities = [Track::class, Album::class, Artist::class, Uri::class], version = 1)
    abstract class PlaylistDatabase : RoomDatabase() {
        abstract fun trackDao(): TrackDao
        abstract fun albumDao(): AlbumDao
        abstract fun artistDao(): ArtistDao
        abstract fun uriDao(): UriDao
    }

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

    data class UriWithTracks(
        @Embedded val uri: Uri,
        @Relation(
            parentColumn = "uriId",
            entityColumn = "trackUriId"
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

    @Dao
    interface UriDao {
        @Insert
        fun insert(vararg uris: Uri)
        @Query("SELECT * FROM uri")
        fun selectAll(): Array<Uri>
    }
}