package com.kuraiji.speedyplaylistcreator.common

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.kuraiji.speedyplaylistcreator.data.PlaylistData

internal fun pushToast(context: Context, mes: CharSequence) {
    Toast.makeText(context, mes, Toast.LENGTH_SHORT).show()
}

internal fun debugLog(mes: String) {
    Log.d("MyDebug", mes)
}

internal fun <T> MutableLiveData<List<T>>.addToList(item: T) {
    if(this.value == null) return
    this.value = this.value!!.toMutableList().apply {
        add(item)
    }
}

internal inline fun <reified T> MutableList<Pair<T, Long>>.splitPair() : MutableList<T> {
    val list: MutableList<T> = mutableListOf()
    this.forEachIndexed { index, pair ->
        list.add(index, pair.first)
    }
    return list
}