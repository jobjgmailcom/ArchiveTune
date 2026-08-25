/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.playback

import java.util.concurrent.ConcurrentHashMap

/** Prevents two automatic Echo Brain cycles from resolving the same playing track at once. */
internal class EchoBrainInjectionGate {
    private val inFlightSeeds = ConcurrentHashMap.newKeySet<String>()

    fun tryAcquire(seedMediaId: String): Boolean =
        seedMediaId.isNotBlank() && inFlightSeeds.add(seedMediaId)

    fun release(seedMediaId: String) {
        inFlightSeeds.remove(seedMediaId)
    }

    fun clear() {
        inFlightSeeds.clear()
    }
}
