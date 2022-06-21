package com.kuraiji.speedyplaylistcreator.domain

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TrackIndexWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
    }
}