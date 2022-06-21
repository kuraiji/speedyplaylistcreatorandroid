package com.kuraiji.speedyplaylistcreator.domain

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuraiji.speedyplaylistcreator.common.debugLog
import com.kuraiji.speedyplaylistcreator.common.notifyObserver
import com.kuraiji.speedyplaylistcreator.data.PlaylistManager
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val uris = MutableLiveData<MutableList<Uri>>(ArrayList())

    fun retrieveUris() {
        val test = viewModelScope.async {
            PlaylistManager.retrieveUris(uris, getApplication())
        }
        test.invokeOnCompletion {
            debugLog("Completed...")
        }
    }

    fun test() {
        uris.value?.add(Uri.EMPTY)
        uris.notifyObserver()
    }
}