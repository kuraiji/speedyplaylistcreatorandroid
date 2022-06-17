package com.kuraiji.speedyplaylistcreator.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.kuraiji.speedyplaylistcreator.common.debugLog

internal const val filename: String = "UriList"

object StorageManager {
    fun storeUri(uri: Uri, context: Context) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { fos ->
            fos.write(uri.toString().toByteArray())
        }
    }

    fun retrieveUris(uris: ArrayList<Uri>, context: Context) {
        context.openFileInput(filename).bufferedReader().useLines { lines ->
            lines.forEach { line ->
                debugLog(line)
                uris.add(line.toUri())
            }
        }
    }
}