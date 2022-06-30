package com.kuraiji.speedyplaylistcreator.data

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.room.*
import androidx.lifecycle.LiveData
import com.kuraiji.speedyplaylistcreator.common.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.RuntimeException

private val IMAGE_NAMES = arrayOf("cover", "front", "folder")
private const val SAVE_NAME = "sav"
private const val SAVE_KEY_BASEDIR = "baseDir"
private const val SAVE_KEY_INITIALLOAD = "initialLoad"

object PlaylistManager {
    suspend fun isDatabaseEmpty(context: Context) : Boolean = withContext(Dispatchers.Default) {
        val db = PlaylistData.PlaylistDatabase.getDatabase(context)
        if(db.uriDao().selectAll().isEmpty()) return@withContext true
        return@withContext false
    }

    fun storeUris(uris: ArrayList<Uri>, context: Context) {
        val db = PlaylistData.PlaylistDatabase.getDatabase(context)
        db.clearAllTables()
        val uriDao = db.uriDao()
        uris.forEach { uri ->
            if(uri.path == null) return@forEach
            uriDao.insert(PlaylistData.Uri(uri.toString(), uri.path!!))
        }
        setInitialScan(context, true)
    }

    fun indexUris(context: Context, callback: (Int)->Unit) {
        val db = PlaylistData.PlaylistDatabase.getDatabase(context)
        val uriDao = db.uriDao()
        val trackDao = db.trackDao()
        val albumDao = db.albumArtistDao()
        uriDao.selectAll().clone().forEachIndexed { index, row ->
            val uri = row.uri.toUri()
            val mmr = MediaMetadataRetriever()
            try {
                mmr.setDataSource(context, uri)
            }
            catch (err: RuntimeException) {
                callback(index)
                uriDao.deleteUris(row)
                return@forEachIndexed
            }
            val trackName: String = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
            var trackNum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER) ?: ""
            var discNum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER) ?: ""
            val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?:
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
            trackNum = trackNum.filter { it.isDigit() }
            discNum = discNum.filter { it.isDigit() }
            try {
                trackDao.insert(PlaylistData.Track(
                    0,
                    trackName,
                    if(trackNum != "") trackNum.toInt() else 0,
                    if(discNum != "") discNum.toInt() else 0,
                    album,
                    artist,
                    row.uri
                ))
            }
            catch(err: SQLiteConstraintException) { }
            try {
                albumDao.insert(PlaylistData.AlbumArtist(
                    album,
                    artist
                ))
            }
            catch (err: SQLiteConstraintException) { }
            try {
                mmr.close()
            }
            catch (err: NoSuchMethodError) {
                mmr.release()
            }
            callback(index)
            uriDao.deleteUris(row)
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

    suspend fun getAlbumTracks(context: Context, albumArtist: PlaylistData.AlbumArtist) : LiveData<Array<PlaylistData.Track>> = withContext(Dispatchers.Default) {
        val db = PlaylistData.PlaylistDatabase.getDatabase(context)
        val trackDao = db.trackDao()
        return@withContext trackDao.getTracksFromAlbumArtist(albumArtist.album, albumArtist.artist)
    }

    fun saveBaseDir(context: Context, uri: Uri) {
        //val sharedPreferences = context.getSharedPreferences(SAVE_NAME, Context.MODE_PRIVATE)
        val sharedPreferences = PlaylistData.PlaylistDatabase.getSharedPreference(context)
        with (sharedPreferences.edit()) {
            putString(SAVE_KEY_BASEDIR, uri.toString())
            apply()
        }
    }

    fun loadBaseDir(context: Context) : Uri {
        //val sharedPreferences = context.getSharedPreferences(SAVE_NAME, Context.MODE_PRIVATE)
        val sharedPreferences = PlaylistData.PlaylistDatabase.getSharedPreference(context)
        val uriString = sharedPreferences.getString(SAVE_KEY_BASEDIR, "") ?: ""
        return if(uriString != "") uriString.toUri() else Uri.EMPTY
    }

    fun setInitialScan(context: Context, state: Boolean) {
        //val sharedPreferences = context.getSharedPreferences(SAVE_NAME, Context.MODE_PRIVATE)
        val sharedPreferences = PlaylistData.PlaylistDatabase.getSharedPreference(context)
        with (sharedPreferences.edit()) {
            putBoolean(SAVE_KEY_INITIALLOAD, state)
            apply()
        }
    }

    fun getInitialScan(context: Context) : Boolean {
        //val sharedPreferences = context.get
        val sharedPreferences = PlaylistData.PlaylistDatabase.getSharedPreference(context)
        return sharedPreferences.getBoolean(SAVE_KEY_INITIALLOAD, false)
    }

    suspend fun savePlaylistToFile(context: Context, tracks: Array<PlaylistData.Track>, fileUri: Uri) = withContext(Dispatchers.Default) {
        val baseDir = loadBaseDir(context)
        try {
            context.contentResolver.openFileDescriptor(fileUri, "wt")?.use {
                FileOutputStream(it.fileDescriptor).use { file ->
                    file.write("#EXTM3U\n".toByteArray())
                    tracks.forEach { track ->
                        val relativePath = track.uri.toUri().path?.replace("${baseDir.path}/", "") ?: return@forEach
                        file.write("#EXTINF:\n$relativePath\n".toByteArray())
                    }
                }
            }
        }
        catch (err: Error) {
            debugLog("Uh Oh, Big Stinky")
        }
    }

    suspend fun loadPlaylistFromFile(context: Context, fileUri: Uri) : Array<PlaylistData.Track>? = withContext(Dispatchers.Default) {
        val db = PlaylistData.PlaylistDatabase.getDatabase(context)
        val trackDao = db.trackDao()
        try {
            val list: MutableList<PlaylistData.Track> = mutableListOf()
            context.contentResolver.openFileDescriptor(fileUri, "r")?.use {
                FileInputStream(it.fileDescriptor).use { file ->
                    file.bufferedReader().use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            if(line[0] != '#') {
                                val track = trackDao.getTrackFromPath("%${line}%")
                                list.add(track)
                            }
                            line = reader.readLine()
                        }
                    }
                }
            }
            return@withContext list.toTypedArray()
        }
        catch (err: Error) {
            debugLog("Uh Oh, Big Stinky")
            return@withContext null
        }
    }
}

class PlaylistData {
    @Entity(indices = [Index(value = ["uri"], unique = true)])
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
        @PrimaryKey
        val uri: String,
        val path: String
    )

    @Database(entities = [Track::class, AlbumArtist::class, Uri::class], version = 1)
    abstract class PlaylistDatabase : RoomDatabase() {
        abstract fun trackDao(): TrackDao
        abstract fun albumArtistDao(): AlbumArtistDao
        abstract fun uriDao(): UriDao

        companion object {
            private var DB_INSTANCE: PlaylistDatabase? = null
            private var SP_INSTANCE: SharedPreferences? = null

            fun getDatabase(context: Context): PlaylistDatabase {
                if(DB_INSTANCE == null) {
                    synchronized(this) {
                        DB_INSTANCE = Room.databaseBuilder(
                            context,
                            PlaylistDatabase::class.java, "PlaylistDatabase"
                        ).fallbackToDestructiveMigration().build()
                    }
                }
                return DB_INSTANCE!!
            }

            fun getSharedPreference(context: Context) : SharedPreferences {
                if(SP_INSTANCE == null) {
                    synchronized(this) {
                        SP_INSTANCE = context.getSharedPreferences(SAVE_NAME, Context.MODE_PRIVATE)
                    }
                }
                return SP_INSTANCE!!
            }
        }
    }

    @Dao
    interface AlbumArtistDao {
        @Insert
        fun insert(vararg albums: AlbumArtist)
        @Query("SELECT * FROM albumartist ORDER BY artist ASC")
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
        @Query("SELECT * FROM track WHERE album LIKE :album AND artist LIKE :artist ORDER BY discNum, trackNum")
        fun getTracksFromAlbumArtist(album: String, artist: String) : LiveData<Array<Track>>
        @Query("SELECT trackId, title, trackNum, discNum, album, artist, Track.uri FROM Track INNER JOIN Uri U ON Track.uri = U.uri WHERE path LIKE :path")
        fun getTrackFromPath(path: String) : Track
    }

    @Dao
    interface UriDao {
        @Insert
        fun insert(vararg uris: Uri)
        @Query("SELECT * FROM uri")
        fun selectAll(): Array<Uri>
        @Query("SELECT COUNT(*) FROM uri")
        fun numOfRows(): Long
        @Query("SELECT COUNT(*) FROM uri")
        fun numOfRowsLiveData() : LiveData<Long>
        @Delete
        fun deleteUris(vararg uris: Uri)
    }
}