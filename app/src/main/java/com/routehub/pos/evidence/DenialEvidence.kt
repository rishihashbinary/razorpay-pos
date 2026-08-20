package com.routehub.pos.evidence

enum class MediaType { PHOTO, AUDIO }

enum class CapturedAbsenceReason { SKIPPED_BY_OPERATOR, CONSENT_DECLINED, CAPABILITY_ABSENT }

data class GeoFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val provider: String,
    val isMock: Boolean,
    val fixTimestamp: Long,
    val fixAgeMs: Long
)

data class MediaItem(
    val type: MediaType,
    val filePath: String?,
    val mimeType: String?,
    val capturedReason: CapturedAbsenceReason?
)


data class DenialEvidence(
    val clientTransactionId: String,
    val reasonCode: String,
    val remarks: String?,
    val geo: GeoFix?,
    val geoTrack: List<GeoFix>,
    val dwellSeconds: Long?,
    val radioFingerprint: RadioFingerprint?,
    val capabilities: CapabilityManifest,
    val media: List<MediaItem>,
    val deviceSerial: String,
    val operatorId: String?,
    val appVersion: String,
    val createdAtMs: Long
)

fun EvidenceLocationTracker.LocationFix.toGeoFix(): GeoFix = GeoFix(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracyMeters,
    provider = provider,
    isMock = isMock,
    fixTimestamp = fixTimestamp,
    fixAgeMs = ageMs()
)