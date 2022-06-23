package com.kuraiji.speedyplaylistcreator.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import android.media.MediaMetadataRetriever
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import com.kuraiji.speedyplaylistcreator.common.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Error
import java.lang.RuntimeException

object PlaylistManager {
    fun wipeDatabase(context: Context) {
        val db = PlaylistData.PlaylistDatabase.getDatabase(context)
        db.clearAllTables()
    }

    fun storeUris(uris: ArrayList<Uri>, context: Context) {
        val db = PlaylistData.PlaylistDatabase.getDatabase(context)
        db.clearAllTables()
        val uriDao = db.uriDao()
        uris.forEach { uri ->
            uriDao.insert(PlaylistData.Uri(uri.toString()))
        }
    }

    fun indexUris(context: Context, callback: (Int)->Unit) {
        val db = PlaylistData.PlaylistDatabase.getDatabase(context)
        val uriDao = db.uriDao()
        val trackDao = db.trackDao()
        val albumDao = db.albumArtistDao()
        uriDao.selectAll().forEachIndexed { index, row ->
            val uri = row.uri.toUri()
            val mmr = MediaMetadataRetriever()
            try {
                mmr.setDataSource(context, uri)
            }
            catch (err: RuntimeException) {
                callback(index)
                return@forEachIndexed
            }
            val trackName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
            val trackNum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER) ?: ""
            val discNum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER) ?: ""
            val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?:
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
            trackDao.insert(PlaylistData.Track(
                0,
                trackName,
                if(trackNum != "") trackNum.toInt() else 0,
                if(discNum != "") discNum.toInt() else 0,
                album,
                artist,
                row.uri
            ))
            try {
                albumDao.insert(PlaylistData.AlbumArtist(
                    album,
                    artist
                ))
            }
            catch (err: SQLiteConstraintException) { }
            mmr.close()
            callback(index)
        }
    }

    fun getNumberOfUris(context: Context) : Long {
        val db = PlaylistData.PlaylistDatabase.getDatabase(context)
        val uriDao = db.uriDao()
        return uriDao.numOfRows()
    }

    fun getAlbums(context: Context) : LiveData<Array<PlaylistData.AlbumArtist>> {
        val db = PlaylistData.PlaylistDatabase.getDatabase(context)
        return db.albumArtistDao().selectAll()
    }
}

class PlaylistData {
    @Entity
    data class Track(
        @PrimaryKey(autoGenerate = true)
        val trackId: Long,
        val title: String,
        val trackNum: Int,
        val discNum: Int,
        val album: String,
        val artist: String,
        val uri: String
    )

    @Entity(primaryKeys = ["album", "artist"])
    data class AlbumArtist(
        val album: String,
        val artist: String
    )

    @Entity
    data class Uri(
        @PrimaryKey()
        val uri: String
    )

    @Database(entities = [Track::class, AlbumArtist::class, Uri::class], version = 1)
    abstract class PlaylistDatabase : RoomDatabase() {
        abstract fun trackDao(): TrackDao
        abstract fun albumArtistDao(): AlbumArtistDao
        abstract fun uriDao(): UriDao

        companion object {
            private var INSTANCE: PlaylistDatabase? = null
            fun getDatabase(context: Context): PlaylistDatabase {
                if(INSTANCE == null) {
                    synchronized(this) {
                        INSTANCE = Room.databaseBuilder(
                            context,
                            PlaylistDatabase::class.java, "PlaylistDatabase"
                        ).fallbackToDestructiveMigration().build()
                    }
                }
                return INSTANCE!!
            }
        }
    }
    /*
    data class AlbumWithTracks(
        @Embedded val album: AlbumArtist,
        @Relation(
            parentColumn = "albumArtistId",
            entityColumn = "trackAlbumArtistId"
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
    */
    @Dao
    interface AlbumArtistDao {
        @Insert
        fun insert(vararg albums: AlbumArtist)
        @Query("SELECT * FROM albumartist")
        fun selectAll() : LiveData<Array<AlbumArtist>>

    }

    @Dao
    interface TrackDao {
        @Insert
        fun insert(vararg tracks: Track)
        @Query("SELECT * FROM track")
        fun selectAll() : LiveData<Array<Track>>
    }

    @Dao
    interface UriDao {
        @Insert
        fun insert(vararg uris: Uri)
        @Query("SELECT * FROM uri")
        fun selectAll(): Array<Uri>
        @Query("SELECT COUNT(*) FROM uri")
        fun numOfRows(): Long
    }
}