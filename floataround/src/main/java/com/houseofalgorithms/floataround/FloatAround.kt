package com.houseofalgorithms.floataround

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import kotlin.math.roundToInt

/**
 * A floating overlay (rail, pill, toolbar) that content should step around.
 *
 * @param gap space kept between the overlay and content beside it.
 * @param feather vertical distance over which content eases in and out of its avoiding position.
 */
@Stable
class FloatingObstacle(
    val gap: Dp = 12.dp,
    val feather: Dp = 140.dp,
) {
    /** Current bounds of the overlay in root coordinates, or null when it is not on screen. */
    var bounds: Rect? by mutableStateOf(null)
        internal set

    /** When false every avoiding element keeps its full width. */
    var enabled: Boolean by mutableStateOf(true)

    internal fun insetPx(row: Rect?, density: Density, layoutDirection: LayoutDirection): Float {
        val obstacleBounds = bounds
        if (!enabled || row == null || obstacleBounds == null) return 0f
        return with(density) {
            startInset(row, obstacleBounds, gap.toPx(), feather.toPx(), layoutDirection == LayoutDirection.Rtl)
        }
    }
}

/**
 * How far [row] must give up its start edge to clear [obstacle].
 *
 * Rows that overlap the obstacle vertically get the full intrusion plus [gapPx]. Rows within
 * [featherPx] of it get a smootherstep-eased share. Rows further away, or already clear of it
 * horizontally, get zero. With [rtl] the obstacle is expected on the right edge.
 */
fun startInset(row: Rect, obstacle: Rect, gapPx: Float, featherPx: Float, rtl: Boolean): Float {
    val intrusion = if (rtl) row.right - obstacle.left + gapPx else obstacle.right + gapPx - row.left
    if (intrusion <= 0f) return 0f
    val verticalDistance = maxOf(obstacle.top - row.bottom, row.top - obstacle.bottom, 0f)
    if (verticalDistance == 0f) return intrusion
    if (verticalDistance >= featherPx) return 0f
    return intrusion * smootherstep(1f - verticalDistance / featherPx)
}

private fun smootherstep(t: Float): Float = t * t * t * (t * (t * 6f - 15f) + 10f)

/** Publishes this element's bounds as the [obstacle]; clears them when the element leaves composition. */
fun Modifier.floatingObstacle(obstacle: FloatingObstacle): Modifier = this then ObstacleElement(obstacle)

private data class ObstacleElement(val obstacle: FloatingObstacle) : ModifierNodeElement<ObstacleNode>() {
    override fun create() = ObstacleNode(obstacle)
    override fun update(node: ObstacleNode) {
        node.obstacle = obstacle
    }
}

private class ObstacleNode(var obstacle: FloatingObstacle) : Modifier.Node(), GlobalPositionAwareModifierNode {
    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        obstacle.bounds = coordinates.boundsInRoot()
    }

    override fun onDetach() {
        obstacle.bounds = null
    }
}

/**
 * Gives up as much of this element's start edge as needed to clear [obstacle], depending on how
 * close the element currently is to it. The end edge stays where it is.
 */
fun Modifier.avoid(obstacle: FloatingObstacle): Modifier = this then AvoidElement(obstacle)

private data class AvoidElement(val obstacle: FloatingObstacle) : ModifierNodeElement<AvoidNode>() {
    override fun create() = AvoidNode(obstacle)
    override fun update(node: AvoidNode) {
        node.obstacle = obstacle
    }
}

private class AvoidNode(var obstacle: FloatingObstacle) :
    Modifier.Node(), LayoutModifierNode, GlobalPositionAwareModifierNode {

    private var rowBounds: Rect? by mutableStateOf(null)

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        rowBounds = coordinates.boundsInRoot()
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val maxInset = if (constraints.hasBoundedWidth) constraints.maxWidth else Int.MAX_VALUE
        val inset = obstacle.insetPx(rowBounds, this, layoutDirection).roundToInt().coerceIn(0, maxInset)
        val placeable = measurable.measure(constraints.offset(horizontal = -inset))
        return layout(constraints.constrainWidth(placeable.width + inset), placeable.height) {
            placeable.placeRelative(inset, 0)
        }
    }
}

/** Tracks one element's distance to an obstacle so its inset can feed padding instead of layout. */
@Stable
class AvoidanceTracker internal constructor(private val obstacle: FloatingObstacle) {
    internal var rowBounds: Rect? by mutableStateOf(null)

    fun inset(density: Density, layoutDirection: LayoutDirection): Dp =
        with(density) { obstacle.insetPx(rowBounds, density, layoutDirection).toDp() }
}

@Composable
fun rememberAvoidanceTracker(obstacle: FloatingObstacle): AvoidanceTracker =
    remember(obstacle) { AvoidanceTracker(obstacle) }

/** Feeds this element's position to [tracker]. */
fun Modifier.track(tracker: AvoidanceTracker): Modifier =
    onGloballyPositioned { tracker.rowBounds = it.boundsInRoot() }

/**
 * Adds the tracked inset to the start padding. Use it as a carousel's content padding so the
 * frame stays full width while the first item steps aside.
 */
fun PaddingValues.plusAvoidance(tracker: AvoidanceTracker, density: Density): PaddingValues =
    AvoidingPaddingValues(this, tracker, density)

private class AvoidingPaddingValues(
    private val base: PaddingValues,
    private val tracker: AvoidanceTracker,
    private val density: Density,
) : PaddingValues {
    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
        val extra = if (layoutDirection == LayoutDirection.Ltr) tracker.inset(density, layoutDirection) else 0.dp
        return base.calculateLeftPadding(layoutDirection) + extra
    }

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
        val extra = if (layoutDirection == LayoutDirection.Rtl) tracker.inset(density, layoutDirection) else 0.dp
        return base.calculateRightPadding(layoutDirection) + extra
    }

    override fun calculateTopPadding(): Dp = base.calculateTopPadding()
    override fun calculateBottomPadding(): Dp = base.calculateBottomPadding()
}
