package moe.rukamori.archivetune.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackStreamRecoveryTrackerTest {
    @Test
    fun allowsOnlyOneRetryUntilPlaybackRecovers() {
        val tracker = PlaybackStreamRecoveryTracker()

        assertTrue(tracker.registerRetryAttempt("video-a"))
        assertFalse(tracker.registerRetryAttempt("video-a"))

        tracker.onPlaybackRecovered("video-a")

        assertTrue(tracker.registerRetryAttempt("video-a"))
    }

    @Test
    fun resetsRetryBudgetWhenThePlayerMovesToAnotherTrack() {
        val tracker = PlaybackStreamRecoveryTracker()

        assertTrue(tracker.registerRetryAttempt("video-a"))
        tracker.onMediaItemChanged("video-b")

        assertTrue(tracker.registerRetryAttempt("video-b"))
    }
}
