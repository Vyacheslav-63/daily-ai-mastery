package com.example

import com.example.ai.AiCurriculumService
import com.example.data.LocalSettingsRepository
import com.example.data.PrebuiltCurriculum
import com.example.data.ProxySettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class RegionalRoutingUnitTest {

    @Test
    fun normalizeProxyBaseUrl_handlesMissingSchemeAndTrailingPaths() {
        assertEquals(
            "https://my-proxy.workers.dev",
            LocalSettingsRepository.normalizeProxyBaseUrl("my-proxy.workers.dev/")
        )
        assertEquals(
            "https://my-proxy.workers.dev",
            LocalSettingsRepository.normalizeProxyBaseUrl("https://my-proxy.workers.dev/v1beta/")
        )
        assertEquals(
            "https://edge.example.com/gemini",
            LocalSettingsRepository.normalizeProxyBaseUrl("https://edge.example.com/gemini/v1beta/models")
        )
        assertEquals(
            "",
            LocalSettingsRepository.normalizeProxyBaseUrl("   ")
        )
    }

    @Test
    fun proxySettings_detectsCustomRouteCorrectly() {
        assertFalse(ProxySettings().isCustomRouteActive)
        assertTrue(ProxySettings(customProxyBaseUrl = "https://worker.dev").isCustomRouteActive)
        assertTrue(ProxySettings(customApiKey = "AIzaTest").isCustomRouteActive)
    }

    @Test
    fun formatRegionalError_guidesUserToProxyWhenGeoBlocked() {
        val geoError = IllegalStateException("User location is not supported for the API use.")
        val defaultRouteMsg = AiCurriculumService.formatRegionalError(geoError, isCustomRouteActive = false)
        assertTrue(defaultRouteMsg.contains("Proxy Base URL"))

        val dnsError = UnknownHostException("generativelanguage.googleapis.com")
        val dnsMsg = AiCurriculumService.formatRegionalError(dnsError, isCustomRouteActive = false)
        assertTrue(dnsMsg.contains("Proxy Base URL"))
    }

    @Test
    fun prebuiltCurriculum_containsDays1To5() {
        for (day in 1L..5L) {
            val lesson = PrebuiltCurriculum.getPrebuiltLesson(day)
            assertTrue("Missing prebuilt lesson for day $day", lesson != null)
            assertTrue(lesson!!.options.size == 4)
        }
    }
}
