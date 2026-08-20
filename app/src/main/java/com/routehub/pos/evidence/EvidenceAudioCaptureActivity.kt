package com.routehub.pos.evidence

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.routehub.pos.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EvidenceAudioCaptureActivity : AppCompatActivity() {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var countDownTimer: CountDownTimer? = null
    private var elapsedSeconds = 0
    private var isRecording = false

    private lateinit var txtTimer: TextView
    private lateinit var btnStop: View

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRecording()
        } else {
            Toast.makeText(this, getString(R.string.mic_permission_required), Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evidence_audio_capture)

        txtTimer = findViewById(R.id.txtTimer)
        btnStop = findViewById(R.id.btnStop)

        btnStop.setOnClickListener { finishRecording() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                discardAndCancel()
            }
        })

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startRecording()
        } else {
            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startRecording() {
        val evidenceDir = File(filesDir, "evidence/audio").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(evidenceDir, "audio_${timestamp}_${System.currentTimeMillis()}.m4a")
        outputFile = file

        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setMaxDuration(MAX_DURATION_MS.toInt())
                setOutputFile(file.absolutePath)

                setOnInfoListener { _, what, _ ->
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        runOnUiThread { finishRecording() }
                    }
                }

                prepare()
                start()
            }

            isRecording = true
            startTimer()

        } catch (e: IOException) {
            Log.e(TAG, "Recorder prepare/start failed", e)
            cleanupFailedFile()
            Toast.makeText(this, getString(R.string.audio_capture_failed), Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Recorder failed", e)
            cleanupFailedFile()
            Toast.makeText(this, getString(R.string.audio_capture_failed), Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(MAX_DURATION_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedSeconds = ((MAX_DURATION_MS - millisUntilFinished) / 1000).toInt()
                txtTimer.text = getString(
                    R.string.recording_timer_format,
                    formatSeconds(elapsedSeconds),
                    formatSeconds(MAX_DURATION_SECONDS)
                )
            }

            override fun onFinish() {
                elapsedSeconds = MAX_DURATION_SECONDS
            }
        }.start()
    }

    private fun finishRecording() {
        if (!isRecording) return
        isRecording = false
        countDownTimer?.cancel()

        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Recorder stop failed", e)
            recorder = null
            cleanupFailedFile()
            Toast.makeText(this, getString(R.string.audio_capture_failed), Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        recorder = null

        val file = outputFile
        if (file == null || !file.exists() || file.length() == 0L) {
            Toast.makeText(this, getString(R.string.audio_capture_failed), Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val result = Intent()
        result.putExtra(EXTRA_AUDIO_PATH, file.absolutePath)
        result.putExtra(EXTRA_AUDIO_DURATION_SECONDS, elapsedSeconds)
        setResult(RESULT_OK, result)
        finish()
    }

    private fun discardAndCancel() {
        countDownTimer?.cancel()
        if (isRecording) {
            try {
                recorder?.apply { stop(); release() }
            } catch (e: Exception) {
            }
        }
        recorder = null
        isRecording = false
        outputFile?.let { if (it.exists()) it.delete() }
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun cleanupFailedFile() {
        try {
            recorder?.release()
        } catch (e: Exception) {
        }
        recorder = null
        outputFile?.let { if (it.exists()) it.delete() }
    }

    private fun formatSeconds(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return String.format(Locale.US, "%d:%02d", m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        if (isRecording) {
            try {
                recorder?.release()
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        const val EXTRA_AUDIO_PATH = "extra_audio_path"
        const val EXTRA_AUDIO_DURATION_SECONDS = "extra_audio_duration_seconds"
        private const val TAG = "EvidenceAudioCapture"
        private const val MAX_DURATION_SECONDS = 30
        private const val MAX_DURATION_MS = MAX_DURATION_SECONDS * 1000L
    }
}