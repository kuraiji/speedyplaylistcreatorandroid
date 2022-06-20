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
import androidx.work.workDataOf
import com.kuraiji.speedyplaylistcreator.R
import com.kuraiji.speedyplaylistcreator.common.debugLog
import com.kuraiji.speedyplaylistcreator.data.PlaylistManager
import kotlin.random.Random

class DirectoryScanWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {
    lateinit var recursiveTraverse: (DocumentFile) -> Unit
    private lateinit var baseDirUri: Uri
    private val uris: MutableLiveData<MutableList<Uri>> = MutableLiveData(ArrayList())

    override suspend fun doWork(): Result {
        baseDirUri = workerParameters.inputData.getString(WorkerKeys.DIR_URI)?.toUri() ?: return Result.failure()
        startForegroundService()
        scanDirectory()
        return Result.success(
            workDataOf(
                WorkerKeys.TRACK_AMT to uris.value?.size
            )
        )
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
        recursiveTraverse = { dFile ->
            if(dFile.isDirectory) {
                dFile.listFiles().forEach { inDFile ->
                    recursiveTraverse(inDFile)
                }
            }
            if(dFile.isFile) {
                dFile.type?.let { mime ->
                    if(mime.contains("audio", true)) {
                        uris.value?.add(dFile.uri)
                    }
                }
            }
        }
        recursiveTraverse(DocumentFile.fromTreeUri(context, baseDirUri) ?: return)
        PlaylistManager.storeUris(ArrayList(uris.value ?: return), context)
    }
}