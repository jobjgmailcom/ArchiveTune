/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Adapted from the GPLv3 Echo Brain source supplied for this fork.
 * The planner is deliberately policy-only: it cannot fetch media, delete a queue, or bypass filters.
 */

package moe.rukamori.archivetune.playback

import androidx.media3.common.MediaItem
import moe.rukamori.archivetune.extensions.metadata
import moe.rukamori.archivetune.models.MediaMetadata
import java.text.Normalizer

/** Pure eligibility and ordering policy for exactly one automatic ArchiveTune recommendation. */
internal object EchoBrainQueuePlanner {
    const val AUTO_TRIGGER_REMAINING_ITEMS = 12

    fun selectOne(
        seed: MediaItem,
        candidates: List<MediaItem>,
        queuedIds: Set<String>,
        injectedIds: Set<String>,
        cooldownKeys: Set<String>,
        recentArtistKeys: Set<String>,
        minimumSimilarity: Int,
        allowAlternativeVersions: Boolean,
    ): MediaItem? {
        val seedMetadata = metadataOf(seed)
        val seedArtistIds = seedMetadata.artists.map(::artistIdentity).toSet()
        val seedAlbumId = seedMetadata.album?.id.orEmpty()

        return candidates
            .asSequence()
            .filter { candidate ->
                val candidateKey = canonicalSongKey(candidate)
                candidate.mediaId.isNotBlank() &&
                    candidate.mediaId !in queuedIds &&
                    candidate.mediaId !in injectedIds &&
                    candidateKey !in cooldownKeys &&
                    (allowAlternativeVersions || !isAlternativeVersion(metadataOf(candidate).title)) &&
                    primaryArtistKey(candidate) !in recentArtistKeys &&
                    similarityScore(candidate, seedArtistIds, seedAlbumId) >= minimumSimilarity
            }
            .distinctBy(MediaItem::mediaId)
            .sortedWith(
                compareByDescending<MediaItem> { similarityScore(it, seedArtistIds, seedAlbumId) }
                    .thenBy { canonicalSongKey(it) }
                    .thenBy(MediaItem::mediaId),
            )
            .distinctBy(::canonicalSongKey)
            .firstOrNull()
    }

    fun shouldAutoInject(
        currentIndex: Int,
        mediaItemCount: Int,
        currentIsEchoBrainRecommendation: Boolean,
        nextIsEchoBrainRecommendation: Boolean,
        hasInjectedRecommendations: Boolean,
        dominantMode: Boolean = true,
    ): Boolean =
        currentIndex in 0 until mediaItemCount &&
            (
                currentIndex == 0 ||
                    (currentIsEchoBrainRecommendation && !nextIsEchoBrainRecommendation) ||
                    (dominantMode && hasInjectedRecommendations && !currentIsEchoBrainRecommendation) ||
                    (!hasInjectedRecommendations && mediaItemCount - currentIndex <= AUTO_TRIGGER_REMAINING_ITEMS)
                )

    fun canonicalSongKey(mediaItem: MediaItem): String {
        val metadata = metadataOf(mediaItem)
        val normalizedTitle = normalize(baseRecordingTitle(metadata.title))
        val normalizedArtists =
            metadata.artists
                .map { normalize(it.name) }
                .filter(String::isNotBlank)
                .sorted()
        return if (normalizedTitle.isBlank() && normalizedArtists.isEmpty()) {
            "id:${normalize(mediaItem.mediaId)}"
        } else {
            "$normalizedTitle|${normalizedArtists.joinToString(",")}"
        }
    }

    fun primaryArtistKey(mediaItem: MediaItem): String =
        normalize(metadataOf(mediaItem).artists.firstOrNull()?.name.orEmpty())
            .ifBlank { canonicalSongKey(mediaItem) }

    private fun metadataOf(mediaItem: MediaItem): MediaMetadata =
        mediaItem.metadata ?: MediaMetadata(
            id = mediaItem.mediaId,
            title = mediaItem.mediaMetadata.title?.toString().orEmpty(),
            artists =
                mediaItem.mediaMetadata.artist
                    ?.toString()
                    ?.takeIf(String::isNotBlank)
                    ?.let { artist -> listOf(MediaMetadata.Artist(id = null, name = artist)) }
                    .orEmpty(),
            duration = 0,
            album =
                mediaItem.mediaMetadata.albumTitle
                    ?.toString()
                    ?.takeIf(String::isNotBlank)
                    ?.let { albumTitle -> MediaMetadata.Album(id = normalize(albumTitle), title = albumTitle) },
        )

    private fun artistIdentity(artist: MediaMetadata.Artist): String = artist.id ?: normalize(artist.name)

    private fun similarityScore(
        candidate: MediaItem,
        seedArtistIds: Set<String>,
        seedAlbumId: String,
    ): Int {
        val metadata = metadataOf(candidate)
        val artistIds = metadata.artists.map(::artistIdentity).toSet()
        var score = 60 // A related/radio result is the baseline relation signal.
        if (artistIds.any { it in seedArtistIds }) score += 30
        if (seedAlbumId.isNotBlank() && metadata.album?.id == seedAlbumId) score += 10
        if (metadata.liked || metadata.inLibrary != null) score += 5
        return score.coerceAtMost(100)
    }

    private fun isAlternativeVersion(title: String): Boolean {
        val normalizedTitle = normalize(title)
        return AlternativeVersionMarkers.any(normalizedTitle::contains)
    }

    private fun baseRecordingTitle(title: String): String =
        title.replace(
            Regex(
                """(?i)\s*[\[(]?\s*(?:\d{4}\s*)?(?:remaster(?:ed)?|radio\s*edit|edit|extended\s*(?:mix|version)?|club\s*mix|album\s*version|deluxe\s*version|single\s*version|mix)\s*[\])]?$""",
            ),
            "",
        )

    private fun normalize(value: String): String =
        Normalizer
            .normalize(value, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
            .filter(Char::isLetterOrDigit)

    private val AlternativeVersionMarkers =
        listOf(
            "remix",
            "live",
            "envivo",
            "acoustic",
            "acustica",
            "acustico",
            "cover",
            "instrumental",
            "session",
            "version",
            "karaoke",
            "slowed",
            "spedup",
            "radioedit",
            "remaster",
            "remastered",
            "extendedmix",
            "clubmix",
            "albumversion",
            "deluxeversion",
            "singleversion",
        )
}
