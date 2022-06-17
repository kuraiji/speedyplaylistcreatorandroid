package com.kuraiji.speedyplaylistcreator.domain

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.kuraiji.speedyplaylistcreator.R
import kotlinx.coroutines.delay
import kotlin.random.Random

import com.kuraiji.speedyplaylistcreator.data.StorageManager

class DirectoryScanWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {
    //var uris: MutableLiveData<MutableList<Uri>> = MutableLiveData(ArrayList())
    lateinit var recursiveTraverse: (DocumentFile) -> Unit
    private lateinit var baseDirUri: Uri

    override suspend fun doWork(): Result {
        baseDirUri = workerParameters.inputData.getString(WorkerKeys.DIR_URI)?.toUri() ?: return Result.failure()
        startForegroundService()
        scanDirectory()
        return Result.success()
    }

    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                NotificationCompat.Builder(context, "scan_channel")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentText("Scanning...")
                    .setContentTitle("Scan in progress")
                    .build()
            )
        )
    }

    private fun scanDirectory() {
        DocumentFile.fromTreeUri(context, baseDirUri)
        recursiveTraverse = { dFile ->
            if(dFile.isDirectory) {
                dFile.listFiles().forEach { inDFile ->
                    recursiveTraverse(inDFile)
                }
            }
            if(dFile.isFile) {
                dFile.type?.let { mime ->
                    if(mime.contains("audio", true)) {
                        //uris.value!!.add(dFile.uri)
                        StorageManager.storeUri(dFile.uri, context)
                    }
                }
            }
        }
    }
}