package com.routehub.pos.evidence

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.routehub.pos.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * In-app photo capture for denial evidence. Deliberately never uses an
 * intent (ACTION_IMAGE_CAPTURE) - a locked-down PAX terminal has no camera
 * app installed to satisfy one. Uses CameraX with whichever camera the
 * device actually has, since the capability probe already confirmed
 * hardware presence before this screen was ever launched.
 *
 * Result contract:
 *   RESULT_OK + EXTRA_PHOTO_PATH -> absolute path of the saved, downscaled JPEG
 *   RESULT_CANCELED -> user backed out, no camera, or permission denied
 */
class EvidencePhotoCaptureActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: View
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evidence_photo_capture)

        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)
        cameraExecutor = Executors.newSingleThreadExecutor()

        findViewById<View>(R.id.btnClose).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        btnCapture.setOnClickListener { takePhoto() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Whichever camera the device actually has - prefer back, fall back to front.
                val cameraSelector = when {
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                    else -> null
                }

                if (cameraSelector == null) {
                    Toast.makeText(this, getString(R.string.no_camera_available), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_CANCELED)
                    finish()
                    return@addListener
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch (e: Exception) {
                Log.e(TAG, "Camera init failed", e)
                Toast.makeText(this, getString(R.string.camera_init_failed), Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return
        btnCapture.isEnabled = false

        val rawFile = File(cacheDir, "evidence_raw_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(rawFile).build()

        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val finalFile = downscaleAndCompress(rawFile)
                    rawFile.delete()

                    runOnUiThread {
                        if (finalFile != null) {
                            val result = Intent()
                            result.putExtra(EXTRA_PHOTO_PATH, finalFile.absolutePath)
                            setResult(RESULT_OK, result)
                            finish()
                        } else {
                            btnCapture.isEnabled = true
                            Toast.makeText(
                                this@EvidencePhotoCaptureActivity,
                                getString(R.string.photo_processing_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Capture failed", exception)
                    runOnUiThread {
                        btnCapture.isEnabled = true
                        Toast.makeText(
                            this@EvidencePhotoCaptureActivity,
                            getString(R.string.photo_capture_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    /**
     * Downscales to a reasonable max dimension and re-compresses to JPEG at
     * moderate quality, so evidence photos don't bloat the upload queue on
     * a field terminal's intermittent LTE. Saved into app-private internal
     * storage, never the public gallery - this is sensitive personal data.
     */
    private fun downscaleAndCompress(rawFile: File): File? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(rawFile.absolutePath, boundsOptions)

            var sampleSize = 1
            while (boundsOptions.outWidth / sampleSize > MAX_DIMENSION_PX ||
                boundsOptions.outHeight / sampleSize > MAX_DIMENSION_PX
            ) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = BitmapFactory.decodeFile(rawFile.absolutePath, decodeOptions) ?: return null

            val evidenceDir = File(filesDir, "evidence/photos").apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val outFile = File(evidenceDir, "photo_${timestamp}_${System.currentTimeMillis()}.jpg")

            FileOutputStream(outFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            bitmap.recycle()

            outFile
        } catch (e: Exception) {
            Log.e(TAG, "Downscale/compress failed", e)
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        const val EXTRA_PHOTO_PATH = "extra_photo_path"
        private const val TAG = "EvidencePhotoCapture"
        private const val MAX_DIMENSION_PX = 1280
        private const val JPEG_QUALITY = 70
    }
}