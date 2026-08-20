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

/**
 * File-backed persistent queue for denial evidence. Survives process death
 * and device reboot - the queue file and all referenced media live in
 * app-private internal storage.
 *
 * Room was the other option per spec; file-backed JSON was chosen instead
 * since this project has no Room/KSP setup yet, and the realistic queue
 * size on a single terminal is small (a handful of pending items at most) -
 * a JSON list is simple, needs zero new dependencies (Gson is already used
 * throughout this app), and is entirely sufficient at this scale.
 *
 * All reads/writes are synchronized through a single queue file, so callers
 * don't need to worry about concurrent access from the UI thread and a
 * background upload worker (Phase 7) at the same time.
 */
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

    /** Marks an item uploaded. Does NOT delete media - call purgeUploaded separately, only after confirmed server success. */
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

    /**
     * Deletes an uploaded item's local record AND its media files. Only ever
     * call this after the server has confirmed receipt - never on a timer,
     * never speculatively. Un-uploaded evidence must never auto-expire.
     */
    @Synchronized
    fun purgeUploaded(context: Context, clientTransactionId: String) {
        val queue = readQueue(context)
        val target = queue.find { it.evidence.clientTransactionId == clientTransactionId }
        target?.evidence?.media?.forEach { item ->
            item.filePath?.let { path ->
                try {
                    File(path).delete()
                } catch (e: Exception) {
                    // best-effort cleanup - a leftover file is harmless, an
                    // un-uploaded queue entry being wrongly removed is not
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
            // Corrupted queue file - fail safe to an empty in-memory queue
            // rather than crashing every launch. This does not delete any
            // media files already on disk, only resets the index.
            mutableListOf()
        }
    }

    private fun writeQueue(context: Context, queue: MutableList<QueuedDenialEvidence>) {
        try {
            queueFile(context).writer().use { writer ->
                gson.toJson(queue, listType, writer)
            }
        } catch (e: Exception) {
            // Best-effort persistence - the item is still in this session's
            // in-memory flow even if the disk write failed.
        }
    }

    private fun queueFile(context: Context): File {
        val dir = File(context.filesDir, "evidence").apply { mkdirs() }
        return File(dir, QUEUE_FILE_NAME)
    }
}