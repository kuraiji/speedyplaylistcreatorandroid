package com.kuraiji.speedyplaylistcreator.domain

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LandingViewModel : ViewModel() {
     var uris: MutableLiveData<MutableList<Uri>> = MutableLiveData(ArrayList())

    fun pushDocFile(documentFile: DocumentFile): Int {
        recursiveTraverse(documentFile)
        //viewModelScope.launch(Dispatchers.IO) {
            //indexTracks(ArrayList(uris.value!!))
        //}
        return uris.value!!.size
    }

    private fun recursiveTraverse(documentFile: DocumentFile) {
        if(documentFile.isDirectory) {
            documentFile.listFiles().forEach { dFile ->
                recursiveTraverse(dFile)
            }
        }
        if(documentFile.isFile) {
            documentFile.type?.let { mime ->
                if(mime.contains("audio", true)) {
                    uris.value!!.add(documentFile.uri)
                }
            }
        }
    }
}