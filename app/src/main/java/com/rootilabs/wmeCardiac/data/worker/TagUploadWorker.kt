package com.rootilabs.wmeCardiac.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rootilabs.wmeCardiac.di.ServiceLocator

class TagUploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "TagUploadWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "TagUploadWorker starting...")
        val repository = ServiceLocator.repository
        
        return try {
            val unsyncedTags = repository.getLocalEventTags().filter { it.isEdit }
            if (unsyncedTags.isEmpty()) {
                Log.d(TAG, "No unsynced tags found.")
                return Result.success()
            }

            Log.d(TAG, "Found ${unsyncedTags.size} unsynced tags. Uploading...")
            
            // First, refresh token to be sure
            repository.getToken()

            val result = repository.uploadVirtualEventTags(unsyncedTags)
            
            if (result.isSuccess) {
                Log.d(TAG, "TagUploadWorker success: all tags uploaded.")
                Result.success()
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "TagUploadWorker failed: ${error?.message}")
                // Retry if it's a network error
                if (isNetworkError(error)) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "TagUploadWorker critical error", e)
            Result.retry()
        }
    }

    private fun isNetworkError(t: Throwable?): Boolean {
        if (t == null) return false
        return t is java.net.UnknownHostException || 
               t is java.net.ConnectException || 
               t is java.net.SocketTimeoutException ||
               t.message?.contains("Unable to resolve host", ignoreCase = true) == true
    }
}
