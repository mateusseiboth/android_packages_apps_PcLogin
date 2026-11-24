package org.lineageos.pclogin.core

import org.junit.Assert.assertEquals
import org.junit.Test

class PodSizeTest {

    @Test
    fun usesLargePodsForAFewUsers() {
        (0..PodSize.LARGE.maxUsers).forEach { count ->
            assertEquals("count $count", PodSize.LARGE, PodSize.forCount(count))
        }
    }

    @Test
    fun usesSmallPodsForMoreUsers() {
        assertEquals(PodSize.SMALL, PodSize.forCount(PodSize.LARGE.maxUsers + 1))
        assertEquals(PodSize.SMALL, PodSize.forCount(40))
    }
}
