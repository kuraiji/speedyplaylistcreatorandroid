package com.kuraiji.speedyplaylistcreator.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.media.MediaMetadataRetriever
import android.os.ParcelFileDescriptor
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.room.*
import androidx.lifecycle.LiveData
import com.kuraiji.speedyplaylistcreator.common.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.RuntimeException

private val IMAGE_NAMES = arrayOf("cover", "front", "folder")

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

    suspend fun getAlbumCover(context: Context, albumArtist: PlaylistData.AlbumArtist) : Bitmap? = withContext(Dispatchers.Default){
        val db = PlaylistData.PlaylistDatabase.getDatabase(context)
        val trackDao = db.trackDao()
        val track = trackDao.getTopTrackFromAlbumArtist(albumArtist.album, albumArtist.artist)
        val uri = track.uri.toUri()
        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(context, uri)
        }
        catch (err: RuntimeException) {
            return@withContext null
        }
        val byteArray = mmr.embeddedPicture
        if(byteArray == null) {
            var dFile = DocumentFile.fromSingleUri(context, uri) ?: return@withContext null
            val uriPaths = dFile.uri.toString().split("%2F")
            val uriDirPath = dFile.uri.toString().replace(uriPaths.last(), "").toUri()
            dFile = DocumentFile.fromTreeUri(context, uriDirPath) ?: return@withContext null
            dFile.listFiles().forEach { file ->
                if(file.type == null || file.name == null) return@forEach
                if(file.type!!.contains("image", true)) {
                    IMAGE_NAMES.forEach { substring ->
                        if(file.name!!.contains(substring)) {
                            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(file.uri, "r")
                            val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                            parcelFileDescriptor?.close()
                            return@withContext image
                        }
                    }
                }
            }
            return@withContext null
        }
        return@withContext BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
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
        @Query("SELECT * FROM track WHERE album LIKE :album AND artist LIKE :artist LIMIT 1")
        fun getTopTrackFromAlbumArtist(album: String, artist: String) : Track
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