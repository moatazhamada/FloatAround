package com.houseofalgorithms.floataround

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class AvoidanceInsetsTest {
    private val leftPill = Rect(left = 12f, top = 400f, right = 92f, bottom = 700f)
    private val rightPill = Rect(left = 908f, top = 400f, right = 988f, bottom = 700f)
    private val gap = 12f
    private val feather = 140f

    private fun row(top: Float, left: Float = 0f, right: Float = 1000f) = Rect(left, top, right, top + 50f)

    private fun insets(row: Rect, obstacle: Rect = leftPill, featherPx: Float = feather) =
        avoidanceInsets(row, obstacle, gap, featherPx)

    @Test
    fun test_rowBesideLeftPill_getsFullIntrusionPlusGapOnLeft() {
        assertEquals(104f, insets(row(500f)).left, 0.01f)
        assertEquals(0f, insets(row(500f)).right, 0.01f)
    }

    @Test
    fun test_rowBesideRightPill_getsFullIntrusionPlusGapOnRight() {
        assertEquals(104f, insets(row(500f), rightPill).right, 0.01f)
        assertEquals(0f, insets(row(500f), rightPill).left, 0.01f)
    }

    @Test
    fun test_rowFarAbovePill_getsNoInset() {
        assertEquals(AvoidanceInsets.None, insets(row(100f)))
    }

    @Test
    fun test_rowAtFeatherEdge_getsNoInset() {
        assertEquals(AvoidanceInsets.None, insets(row(210f)))
    }

    @Test
    fun test_rowHalfwayThroughFeather_getsHalfInset() {
        assertEquals(52f, insets(row(280f)).left, 0.01f)
    }

    @Test
    fun test_rowAlreadyClearOfPill_getsNoInset() {
        assertEquals(AvoidanceInsets.None, insets(row(500f, left = 200f)))
    }

    @Test
    fun test_zeroFeather_onlyOverlappingRowsInset() {
        assertEquals(104f, insets(row(500f), featherPx = 0f).left, 0.01f)
        assertEquals(AvoidanceInsets.None, insets(row(330f), featherPx = 0f))
    }
}
