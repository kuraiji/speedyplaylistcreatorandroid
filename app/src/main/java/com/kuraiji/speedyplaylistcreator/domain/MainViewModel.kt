package com.kuraiji.speedyplaylistcreator.domain

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
import com.kuraiji.speedyplaylistcreator.common.debugLog
import com.kuraiji.speedyplaylistcreator.common.notifyObserver
import com.kuraiji.speedyplaylistcreator.data.PlaylistData
import com.kuraiji.speedyplaylistcreator.data.PlaylistManager
import com.kuraiji.speedyplaylistcreator.ui.MainActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val WORKNAME = "index"

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val albums = PlaylistData.PlaylistDatabase.getDatabase(application).albumArtistDao().selectAll()
    val tracks = PlaylistData.PlaylistDatabase.getDatabase(application).trackDao().selectAll()

    init {
        fullIndex()
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
}