package com.routehub.pos.evidence

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

enum class UploadStatus { PENDING, UPLOADED }

data class QueuedDenialEvidence(
    val evidence: DenialEvidence,
    val status: UploadStatus,
    val attemptCount: Int = 0,
    val lastAttemptAtMs: Long? = null
)

object DenialEvidenceQueue {

    private const val QUEUE_FILE_NAME = "denial_evidence_queue.json"
    private val gson = Gson()
    private val listType = object : TypeToken<MutableList<QueuedDenialEvidence>>() {}.type

    @Synchronized
    fun enqueue(context: Context, evidence: DenialEvidence) {
        val queue = readQueue(context)
        queue.add(QueuedDenialEvidence(evidence = evidence, status = UploadStatus.PENDING))
        writeQueue(context, queue)
    }

    @Synchronized
    fun getPending(context: Context): List<QueuedDenialEvidence> {
        return readQueue(context).filter { it.status == UploadStatus.PENDING }
    }

    @Synchronized
    fun getPendingCount(context: Context): Int {
        return getPending(context).size
    }
    @Synchronized
    fun markUploaded(context: Context, clientTransactionId: String) {
        val queue = readQueue(context)
        val updated = queue.map {
            if (it.evidence.clientTransactionId == clientTransactionId) {
                it.copy(status = UploadStatus.UPLOADED)
            } else {
                it
            }
        }.toMutableList()
        writeQueue(context, updated)
    }

    @Synchronized
    fun recordAttempt(context: Context, clientTransactionId: String) {
        val queue = readQueue(context)
        val updated = queue.map {
            if (it.evidence.clientTransactionId == clientTransactionId) {
                it.copy(attemptCount = it.attemptCount + 1, lastAttemptAtMs = System.currentTimeMillis())
            } else {
                it
            }
        }.toMutableList()
        writeQueue(context, updated)
    }
    @Synchronized
    fun purgeUploaded(context: Context, clientTransactionId: String) {
        val queue = readQueue(context)
        val target = queue.find { it.evidence.clientTransactionId == clientTransactionId }
        target?.evidence?.media?.forEach { item ->
            item.filePath?.let { path ->
                try {
                    File(path).delete()
                } catch (e: Exception) {
                }
            }
        }
        val updated = queue.filterNot { it.evidence.clientTransactionId == clientTransactionId }.toMutableList()
        writeQueue(context, updated)
    }

    private fun readQueue(context: Context): MutableList<QueuedDenialEvidence> {
        val file = queueFile(context)
        if (!file.exists()) return mutableListOf()

        return try {
            file.reader().use { reader ->
                gson.fromJson<MutableList<QueuedDenialEvidence>>(reader, listType) ?: mutableListOf()
            }
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun writeQueue(context: Context, queue: MutableList<QueuedDenialEvidence>) {
        try {
            queueFile(context).writer().use { writer ->
                gson.toJson(queue, listType, writer)
            }
        } catch (e: Exception) {
        }
    }

    private fun queueFile(context: Context): File {
        val dir = File(context.filesDir, "evidence").apply { mkdirs() }
        return File(dir, QUEUE_FILE_NAME)
    }
}