package com.houseofalgorithms.floataround

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class StartInsetTest {
    private val pill = Rect(left = 12f, top = 400f, right = 92f, bottom = 700f)
    private val gap = 12f
    private val feather = 140f

    private fun row(top: Float, left: Float = 0f, right: Float = 1000f) = Rect(left, top, right, top + 50f)

    @Test
    fun test_rowBesidePill_getsFullIntrusionPlusGap() {
        assertEquals(104f, startInset(row(500f), pill, gap, feather, rtl = false), 0.01f)
    }

    @Test
    fun test_rowFarAbovePill_getsNoInset() {
        assertEquals(0f, startInset(row(100f), pill, gap, feather, rtl = false), 0.01f)
    }

    @Test
    fun test_rowAtFeatherEdge_getsNoInset() {
        assertEquals(0f, startInset(row(210f), pill, gap, feather, rtl = false), 0.01f)
    }

    @Test
    fun test_rowHalfwayThroughFeather_getsHalfInset() {
        assertEquals(52f, startInset(row(280f), pill, gap, feather, rtl = false), 0.01f)
    }

    @Test
    fun test_rowAlreadyRightOfPill_getsNoInset() {
        assertEquals(0f, startInset(row(500f, left = 200f), pill, gap, feather, rtl = false), 0.01f)
    }

    @Test
    fun test_rtlPillOnRightEdge_insetsFromRight() {
        val rtlPill = Rect(left = 908f, top = 400f, right = 988f, bottom = 700f)
        assertEquals(104f, startInset(row(500f), rtlPill, gap, feather, rtl = true), 0.01f)
    }

    @Test
    fun test_zeroFeather_onlyOverlappingRowsInset() {
        assertEquals(104f, startInset(row(500f), pill, gap, 0f, rtl = false), 0.01f)
        assertEquals(0f, startInset(row(330f), pill, gap, 0f, rtl = false), 0.01f)
    }
}
