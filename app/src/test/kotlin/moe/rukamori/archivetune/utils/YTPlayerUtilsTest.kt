package moe.rukamori.archivetune.utils

import moe.rukamori.archivetune.constants.PlayerStreamClient
import moe.rukamori.archivetune.innertube.PlaybackAuthState
import moe.rukamori.archivetune.innertube.models.YouTubeClient.Companion.ANDROID_VR_1_65_10
import moe.rukamori.archivetune.innertube.models.YouTubeClient.Companion.VISIONOS
import org.junit.Assert.assertTrue
import org.junit.Test

class YTPlayerUtilsTest {
    @Test
    fun visionOsDirectFallbackPrecedesAndroidVr() {
        val clients =
            YTPlayerUtils.buildStreamClientOrder(
                preferredStreamClient = PlayerStreamClient.WEB_REMIX,
                authState = PlaybackAuthState.EMPTY,
            )

        assertTrue(clients.indexOf(VISIONOS) >= 0)
        assertTrue(clients.indexOf(ANDROID_VR_1_65_10) >= 0)
        assertTrue(clients.indexOf(VISIONOS) < clients.indexOf(ANDROID_VR_1_65_10))
    }
}
