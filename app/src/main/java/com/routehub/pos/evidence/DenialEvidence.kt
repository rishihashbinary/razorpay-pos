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

/**
 * The full evidence bundle shipped for a single denial, keyed by a
 * device-generated clientTransactionId. This is the exact shape the server
 * expects in POST /denial-evidence (Phase 7's job to actually send it).
 */
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

/** fixAgeMs is frozen here, at bundle-creation time - not recomputed later,
 *  since this bundle may sit in the offline queue for a while before upload. */
fun EvidenceLocationTracker.LocationFix.toGeoFix(): GeoFix = GeoFix(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracyMeters,
    provider = provider,
    isMock = isMock,
    fixTimestamp = fixTimestamp,
    fixAgeMs = ageMs()
)