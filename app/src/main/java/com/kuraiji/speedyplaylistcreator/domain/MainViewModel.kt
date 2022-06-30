package com.kuraiji.speedyplaylistcreator.domain

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.kuraiji.speedyplaylistcreator.common.debugLog
import com.kuraiji.speedyplaylistcreator.common.splitPair
import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.data.PlaylistManager
import com.kuraiji.speedyplaylistcreator.ui.MainActivity
import kotlinx.coroutines.launch

private const val WORKNAME = "index"

class MainViewModel(application: Application) : AndroidViewModel(application) {
    companion object ViewKeys {
        const val VIEW_KEY_ALBUM: Int = 0
        const val VIEW_KEY_TRACK: Int = 1
        const val VIEW_KEY_PLAYLIST: Int = 2
        const val VIEW_KEY_SETTINGS: Int = 3
    }
    //private val _currentView = MutableLiveData(VIEW_KEY_ALBUM)
    private val _currentView = mutableStateOf(VIEW_KEY_ALBUM)
    val currentView: State<Int> = _currentView
    private val _navigation = mutableStateListOf<Short>(0)

    val albums = PlaylistData.PlaylistDatabase.getDatabase(application).albumArtistDao().selectAll()
    private val _albumIndex = mutableStateOf(0)
    val albumIndex: State<Int> = _albumIndex
    private val _albumoffset = mutableStateOf(0)
    val albumoffset: State<Int> = _albumoffset

    private val testAlbums = mutableStateListOf<PlaylistData.AlbumArtist>()
    val testAl: List<PlaylistData.AlbumArtist> = testAlbums

    private val _playlist = mutableStateMapOf<String, Pair<PlaylistData.Track, Long>>()
    private val _playlistIndex = mutableStateOf<Long>(0)
    val playlist: Map<String, Pair<PlaylistData.Track, Long>> = _playlist

    private val _baseDirUri = mutableStateOf(PlaylistManager.loadBaseDir(getApplication()))
    val baseDirUri: State<Uri> = _baseDirUri

    init {
        fullIndex()
        viewModelScope.launch {
            PlaylistData.PlaylistDatabase.getDatabase(application).albumArtistDao().selectAll().observeForever { array ->
                testAlbums.clear()
                array.forEach { albumArtist ->
                    testAlbums.add(albumArtist)
                }
            }
        }
    }

    fun savePlaylist(fileUri: Uri) {
        viewModelScope.launch {
            val trackList = _playlist.values.toMutableList()
            trackList.sortBy { it.second }
            PlaylistManager.savePlaylistToFile(getApplication(), trackList.splitPair().toTypedArray(), fileUri)
        }
    }
    fun loadPlaylist(fileUri: Uri) {
        viewModelScope.launch {
            val loadedTracks = PlaylistManager.loadPlaylistFromFile(getApplication(), fileUri) ?: return@launch
            _playlist.clear()
            _playlistIndex.value = 0
            loadedTracks.forEach { track ->
                addToPlaylist(track)
            }
        }
    }

    private fun fullIndex() {
        val indexRequest = OneTimeWorkRequestBuilder<IndexTracksWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiresStorageNotLow(true)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        val workManager = WorkManager.getInstance(getApplication())
        workManager.pruneWork()
        workManager.beginUniqueWork(WORKNAME, ExistingWorkPolicy.KEEP,indexRequest).enqueue()
    }

    fun toggleAddToPlaylist(track: PlaylistData.Track) {
        when(_playlist.containsKey(track.uri)) {
            false -> _playlist[track.uri] = Pair(track, _playlistIndex.value++)
            true -> _playlist.remove(track.uri)
        }
    }

    fun addToPlaylist(track: PlaylistData.Track) {
        if(_playlist.containsKey(track.uri)) return
        _playlist[track.uri] = Pair(track, _playlistIndex.value++)
    }

    fun removeFromPlaylist(track: PlaylistData.Track) {
        if(!_playlist.containsKey(track.uri)) return
        _playlist.remove(track.uri)
    }

    private fun checkLastVisitedTrackAlbumView() : Int {
        var firstDone = false
        return run<Int> viewSearch@ {
            _navigation.asReversed().forEach {  view ->
                if(!firstDone) {
                    firstDone = true
                    return@forEach
                }
                if(view < 0 || view > 1) return@forEach
                return@viewSearch view.toInt()
            }
            return@viewSearch 0
        }
    }

    fun goToView(view: Int) {
        when(view) {
            VIEW_KEY_PLAYLIST -> {
                _currentView.value = view
                _navigation.add(view.toShort())
            }
            VIEW_KEY_SETTINGS -> {
                _currentView.value = view
                _navigation.add(view.toShort())
            }
            VIEW_KEY_TRACK -> {
                _currentView.value = view
                _navigation.add(view.toShort())
            }
            VIEW_KEY_ALBUM -> {
                if(checkLastVisitedTrackAlbumView() == VIEW_KEY_TRACK) {
                    _navigation.clear()
                    _navigation.add(0)
                    _currentView.value = VIEW_KEY_TRACK
                    _navigation.add(VIEW_KEY_TRACK.toShort())
                }
                else {
                    _navigation.clear()
                    _navigation.add(view.toShort())
                    _currentView.value = view
                }
            }
        }
    }

    fun goBack() {
        if(_navigation.last() == VIEW_KEY_ALBUM.toShort()) return
        _navigation.removeLastOrNull()
        _currentView.value = _navigation.last().toInt()
    }

    fun setAlbumIndex(index: Int, offset: Int) {
        _albumIndex.value = index
        _albumoffset.value = offset
    }
}