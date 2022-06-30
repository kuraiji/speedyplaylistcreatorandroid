package com.kuraiji.speedyplaylistcreator.domain

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.kuraiji.speedyplaylistcreator.R
import com.kuraiji.speedyplaylistcreator.common.debugLog
import com.kuraiji.speedyplaylistcreator.data.PlaylistManager
import kotlin.random.Random

class IndexTracksWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    private var trackAmt: Long = 0
    private lateinit var notification: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManagerCompat
    private var notId: Int = 0

    override suspend fun doWork(): Result {
        trackAmt = PlaylistManager.getNumberOfUris(context)
        notId = Random.nextInt()
        notificationManager = NotificationManagerCompat.from(context)
        startForegroundService()
        PlaylistManager.indexUris(context, ::updateProgress)
        return Result.success()
    }

    private suspend fun startForegroundService() {
        notification = NotificationCompat.Builder(context, "scan_channel")
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setContentText("Indexing Tracks...")
            .setContentTitle("Indexing in progress")
            .setProgress(trackAmt.toInt(), 0, false)
            .setOngoing(true)
        setForeground(
            ForegroundInfo(
                notId,
                notification.build()
            )
        )
    }

    private fun updateProgress(num: Int) {
        notification.setProgress(trackAmt.toInt(), num, false)
        if((num + 1) >= trackAmt) notification.setOngoing(false)
        notificationManager.notify(notId, notification.build())
    }
}