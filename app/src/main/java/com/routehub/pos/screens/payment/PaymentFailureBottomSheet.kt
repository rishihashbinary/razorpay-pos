package com.routehub.pos.screens.payment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.routehub.pos.R
import com.routehub.pos.clients.ApiClient
import com.routehub.pos.clients.SessionManager
import com.routehub.pos.evidence.CapabilityManifest
import com.routehub.pos.evidence.CapturedAbsenceReason
import com.routehub.pos.evidence.DenialEvidence
import com.routehub.pos.evidence.DeviceContext
import com.routehub.pos.evidence.EvidenceAudioCaptureActivity
import com.routehub.pos.evidence.EvidenceLocationTracker
import com.routehub.pos.evidence.EvidencePhotoCaptureActivity
import com.routehub.pos.evidence.MediaItem
import com.routehub.pos.evidence.MediaType
import com.routehub.pos.evidence.RadioFingerprint
import com.routehub.pos.evidence.RadioFingerprintCapture
import com.routehub.pos.evidence.toGeoFix
import com.routehub.pos.models.Reason
import com.routehub.pos.models.responses.ApiListResponse
import com.routehub.pos.services.PaymentService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class PaymentFailureBottomSheet(
    private val locationTracker: EvidenceLocationTracker,
    private val capabilities: CapabilityManifest,
    private val onSubmit: (evidence: DenialEvidence) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var edtRemarks: EditText
    private lateinit var btnSubmit: Button

    private lateinit var txtLocationChip: TextView
    private lateinit var evidenceSection: View
    private lateinit var txtEvidenceHeading: TextView
    private lateinit var chipAddPhoto: TextView
    private lateinit var chipVoiceNote: TextView

    private var photoPath: String? = null
    private var audioPath: String? = null
    private var audioCapturedReason: String? = null
    private var radioFingerprint: RadioFingerprint? = null
    private var dwellSeconds: Long? = null
    private var geoTrack: List<EvidenceLocationTracker.LocationFix> = emptyList()

    val paymentService = ApiClient.retrofit.create(PaymentService::class.java)

    private val photoCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val path = result.data?.getStringExtra(EvidencePhotoCaptureActivity.EXTRA_PHOTO_PATH)
            if (path != null) {
                photoPath = path
                setChipState(chipAddPhoto, attached = true, label = getString(R.string.photo_added))
            }
        }
    }


    private val audioCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val path = result.data?.getStringExtra(EvidenceAudioCaptureActivity.EXTRA_AUDIO_PATH)
            val duration = result.data?.getIntExtra(
                EvidenceAudioCaptureActivity.EXTRA_AUDIO_DURATION_SECONDS, 0
            ) ?: 0
            if (path != null) {
                audioPath = path
                setChipState(
                    chipVoiceNote,
                    attached = true,
                    label = getString(R.string.audio_added_format, formatDuration(duration))
                )
            }
        }

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.bottomsheet_payment_failure, null)

        radioGroup = view.findViewById(R.id.radioGroupReasons)
        edtRemarks = view.findViewById(R.id.edtRemarks)
        btnSubmit = view.findViewById(R.id.btnSubmitFailure)
        txtLocationChip = view.findViewById(R.id.txtLocationChip)
        evidenceSection = view.findViewById(R.id.evidenceSection)
        txtEvidenceHeading = view.findViewById(R.id.txtEvidenceHeading)
        chipAddPhoto = view.findViewById(R.id.chipAddPhoto)
        chipVoiceNote = view.findViewById(R.id.chipVoiceNote)

        setupRadioButtons()
        setupLocationChip()
        setupEvidenceSection()

        btnSubmit.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId

            if (selectedId == -1) {
                Toast.makeText(context, "Please select a reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadio = view.findViewById<RadioButton>(selectedId)
            val reason = selectedRadio.tag.toString()
            val remarks = edtRemarks.text.toString()
            radioFingerprint = RadioFingerprintCapture.capture(requireContext())
            dwellSeconds = locationTracker.getDwellSeconds()
            geoTrack = locationTracker.getTrack()
            onSubmit(buildDenialEvidence(reason, remarks))
            dismiss()
        }

        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(view)
        return dialog
    }

    private fun buildDenialEvidence(reason: String, remarks: String): DenialEvidence {
        val bestFix = locationTracker.getBestFix()

        val photoItem = if (photoPath != null) {
            MediaItem(MediaType.PHOTO, photoPath, "image/jpeg", null)
        } else {
            val absenceReason = if (!capabilities.hasCamera) {
                CapturedAbsenceReason.CAPABILITY_ABSENT
            } else {
                CapturedAbsenceReason.SKIPPED_BY_OPERATOR
            }
            MediaItem(MediaType.PHOTO, null, null, absenceReason)
        }

        val audioItem = if (audioPath != null) {
            MediaItem(MediaType.AUDIO, audioPath, "audio/mp4", null)
        } else {
            val absenceReason = when {
                !capabilities.hasMic -> CapturedAbsenceReason.CAPABILITY_ABSENT
                audioCapturedReason == "CONSENT_DECLINED" -> CapturedAbsenceReason.CONSENT_DECLINED
                else -> CapturedAbsenceReason.SKIPPED_BY_OPERATOR
            }
            MediaItem(MediaType.AUDIO, null, null, absenceReason)
        }

        return DenialEvidence(
            clientTransactionId = UUID.randomUUID().toString(),
            reasonCode = reason,
            remarks = remarks,
            geo = bestFix?.toGeoFix(),
            geoTrack = geoTrack.map { it.toGeoFix() },
            dwellSeconds = dwellSeconds,
            radioFingerprint = radioFingerprint,
            capabilities = capabilities,
            media = listOf(photoItem, audioItem),
            deviceSerial = DeviceContext.getDeviceIdentifier(requireContext()),
            operatorId = SessionManager.getUserId(),
            appVersion = DeviceContext.getAppVersion(requireContext()),
            createdAtMs = System.currentTimeMillis()
        )
    }

    private fun setupRadioButtons() {

        paymentService.getDenialReasons().enqueue(object : Callback<ApiListResponse<Reason>> {
            override fun onResponse(call: Call<ApiListResponse<Reason>>, response: Response<ApiListResponse<Reason>>) {
                if (response.isSuccessful) {
                    val reasons = response.body()?.data as List<Reason>
                    reasons.forEach { reason ->
                        val radioButton = RadioButton(context)
                        radioButton.text = reason.reason
                        radioButton.tag = reason.reason
                        radioGroup.addView(radioButton)
                    }
                }
            }

            override fun onFailure(call: Call<ApiListResponse<Reason>>, t: Throwable) {
                t.printStackTrace()
            }
        });
    }
    private fun setupLocationChip() {
        val fix = locationTracker.getBestFix()
        txtLocationChip.text = when {
            !capabilities.hasLocationPermission -> getString(R.string.location_unavailable)
            fix == null -> getString(R.string.location_acquiring)
            else -> getString(R.string.location_captured_format, formatAccuracy(fix.accuracyMeters))
        }
    }

    private fun formatAccuracy(accuracyMeters: Float): String {
        if (accuracyMeters < 0) return "?"
        return accuracyMeters.toInt().toString()
    }

    private fun setupEvidenceSection() {
        val anyEvidenceAvailable = capabilities.hasCamera || capabilities.hasMic
        evidenceSection.visibility = if (anyEvidenceAvailable) View.VISIBLE else View.GONE

        chipAddPhoto.visibility = if (capabilities.hasCamera) View.VISIBLE else View.GONE
        chipVoiceNote.visibility = if (capabilities.hasMic) View.VISIBLE else View.GONE

        val hasMultipleOptions = capabilities.hasCamera && capabilities.hasMic
        txtEvidenceHeading.visibility = if (hasMultipleOptions) View.VISIBLE else View.GONE

        chipAddPhoto.setOnClickListener { onPhotoChipTapped() }
        chipVoiceNote.setOnClickListener { onVoiceChipTapped() }
    }

    private fun onPhotoChipTapped() {
        if (photoPath != null) {
            confirmRemoval(getString(R.string.remove_photo_confirm)) {
                photoPath = null
                setChipState(chipAddPhoto, attached = false, label = getString(R.string.add_photo))
            }
            return
        }

        photoCaptureLauncher.launch(Intent(requireContext(), EvidencePhotoCaptureActivity::class.java))
    }

    private fun onVoiceChipTapped() {
        if (audioPath != null) {
            confirmRemoval(getString(R.string.remove_audio_confirm)) {
                audioPath = null
                audioCapturedReason = null
                setChipState(chipVoiceNote, attached = false, label = getString(R.string.voice_note))
            }
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.voice_consent_title))
            .setMessage(getString(R.string.voice_consent_message))
            .setPositiveButton(getString(R.string.consent_and_record)) { _, _ ->
                audioCapturedReason = null
                audioCaptureLauncher.launch(Intent(requireContext(), EvidenceAudioCaptureActivity::class.java))
            }
            .setNegativeButton(getString(R.string.decline)) { _, _ ->
                audioCapturedReason = "CONSENT_DECLINED"
                Toast.makeText(requireContext(), "Voice note skipped", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun formatDuration(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return String.format(java.util.Locale.US, "%d:%02d", m, s)
    }

    private fun confirmRemoval(message: String, onConfirmed: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(getString(R.string.remove)) { _, _ -> onConfirmed() }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setChipState(chip: TextView, attached: Boolean, label: String) {
        chip.text = label
        chip.setBackgroundResource(
            if (attached) R.drawable.bg_chip_attached else R.drawable.bg_chip_neutral
        )
//        PaymentFailureReason.values().forEach { reason ->
//            val radioButton = RadioButton(context)
//            radioButton.text = reason.displayName
//            radioButton.tag = reason.name
//            radioGroup.addView(radioButton)
//        }
    }
}