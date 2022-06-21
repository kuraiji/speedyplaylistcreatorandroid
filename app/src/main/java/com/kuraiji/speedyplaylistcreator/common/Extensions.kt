package com.kuraiji.speedyplaylistcreator.common

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData

internal fun pushToast(context: Context, mes: CharSequence) {
    Toast.makeText(context, mes, Toast.LENGTH_SHORT).show()
}

internal fun debugLog(mes: String) {
    Log.d("MyDebug", mes)
}

internal fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}