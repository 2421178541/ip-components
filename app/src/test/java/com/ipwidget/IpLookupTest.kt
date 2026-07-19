package com.ipwidget

import org.junit.Assert.assertTrue
import org.junit.Test

class IpLookupTest {
    @Test
    fun fetchIpAddress_returnsNonEmptyStringForPublicEndpoint() {
        val result = IpLookup.fetchIpAddress("https://api.ipify.org")
        assertTrue(result.isNotEmpty())
    }
}
