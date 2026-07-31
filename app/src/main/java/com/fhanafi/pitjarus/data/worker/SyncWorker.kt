package com.fhanafi.pitjarus.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fhanafi.pitjarus.data.sync.PendingActionSyncer
import com.fhanafi.pitjarus.data.sync.SyncOutcome
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val pendingActionSyncer: PendingActionSyncer
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        Timber.d("Worker execution: sync pending actions")
        return when (pendingActionSyncer.sync()) {
            SyncOutcome.Success -> Result.success()
            SyncOutcome.Retry -> Result.retry()
        }
    }
}
