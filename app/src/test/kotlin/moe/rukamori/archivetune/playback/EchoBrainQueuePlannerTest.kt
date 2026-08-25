/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.playback

import androidx.media3.common.MediaItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EchoBrainQueuePlannerTest {
    @Test
    fun `strict similarity chooses only a shared-artist candidate`() {
        val seed = item("seed", "Anchor", "artist-a")
        val sameArtist = item("same", "Related", "artist-a")
        val unrelated = item("other", "Other", "artist-b")

        val selected =
            EchoBrainQueuePlanner.selectOne(
                seed = seed,
                candidates = listOf(unrelated, sameArtist),
                queuedIds = setOf(seed.mediaId),
                injectedIds = emptySet(),
                cooldownKeys = emptySet(),
                recentArtistKeys = emptySet(),
                minimumSimilarity = 90,
                allowAlternativeVersions = false,
            )

        assertEquals("same", selected?.mediaId)
    }

    @Test
    fun `alternative and cooldown candidates remain blocked`() {
        val seed = item("seed", "Anchor", "artist-a")
        val remix = item("remix", "Anchor Remix", "artist-a")
        val cooldown = item("cooldown", "Related", "artist-a")

        val selected =
            EchoBrainQueuePlanner.selectOne(
                seed = seed,
                candidates = listOf(remix, cooldown),
                queuedIds = setOf(seed.mediaId),
                injectedIds = emptySet(),
                cooldownKeys = setOf(EchoBrainQueuePlanner.canonicalSongKey(cooldown)),
                recentArtistKeys = emptySet(),
                minimumSimilarity = 90,
                allowAlternativeVersions = false,
            )

        assertEquals(null, selected)
    }

    @Test
    fun `dominant mode refills after an original track while a non-dominant cycle does not`() {
        assertTrue(
            EchoBrainQueuePlanner.shouldAutoInject(
                currentIndex = 3,
                mediaItemCount = 20,
                currentIsEchoBrainRecommendation = false,
                nextIsEchoBrainRecommendation = false,
                hasInjectedRecommendations = true,
                dominantMode = true,
            ),
        )
        assertFalse(
            EchoBrainQueuePlanner.shouldAutoInject(
                currentIndex = 3,
                mediaItemCount = 20,
                currentIsEchoBrainRecommendation = false,
                nextIsEchoBrainRecommendation = false,
                hasInjectedRecommendations = true,
                dominantMode = false,
            ),
        )
    }

    @Test
    fun `gate allows one in-flight cycle per seed`() {
        val gate = EchoBrainInjectionGate()

        assertTrue(gate.tryAcquire("seed"))
        assertFalse(gate.tryAcquire("seed"))
        gate.release("seed")
        assertTrue(gate.tryAcquire("seed"))
    }

    private fun item(id: String, title: String, artistId: String): MediaItem =
        MediaItem
            .Builder()
            .setMediaId(id)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata
                    .Builder()
                    .setTitle(title)
                    .setArtist(artistId)
                    .build(),
            ).build()
}
