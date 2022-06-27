package com.kuraiji.speedyplaylistcreator.domain

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import androidx.work.*
import com.kuraiji.speedyplaylistcreator.common.splitPair
import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.data.PlaylistManager
import kotlinx.coroutines.launch

private const val WORKNAME = "index"

class MainViewModel(application: Application) : AndroidViewModel(application) {
    companion object ViewKeys {
        val VIEW_KEY_ALBUM: Int = 0
        val VIEW_KEY_TRACK: Int = 1
    }
    val currentView = MutableLiveData(VIEW_KEY_ALBUM)
    val albums = PlaylistData.PlaylistDatabase.getDatabase(application).albumArtistDao().selectAll()
    private val _playlist = mutableStateMapOf<String, Pair<PlaylistData.Track, Long>>()
    private val _playlistIndex = mutableStateOf<Long>(0)
    val playlist: Map<String, Pair<PlaylistData.Track, Long>> = _playlist

    init {
        fullIndex()
    }

    fun savePlaylist() {
        viewModelScope.launch {
            val trackList = _playlist.values.toMutableList()
            trackList.sortBy { it.second }
            PlaylistManager.savePlaylistToFile(getApplication(), trackList.splitPair().toTypedArray())
        }
    }
    fun loadPlaylist() {
        viewModelScope.launch {
            val loadedTracks = PlaylistManager.loadPlaylistFromFile(getApplication()) ?: return@launch
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
}