package com.dev.headphoneplayer.ui.component

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dev.headphoneplayer.util.audio.VisualizerData
import com.dev.headphoneplayer.util.audio.VisualizerHelper
import kotlin.math.roundToInt

@JvmInline
private value class Point(private val p: Pair<Float, Float>) {

    fun x() = p.first
    fun y() = p.second

    operator fun plus(other: Point) = Point(x() + other.x() to y() + other.y())
    operator fun minus(other: Point) = Point(x() - other.x() to y() - other.y())
    operator fun times(factor: Float) = Point(x() * factor to y() * factor)
    operator fun div(factor: Float) = times(1f / factor)

}

@Composable
fun StackedBarVisualizer(
    data: VisualizerData,
    barCount: Int,
    modifier: Modifier = Modifier,
    maxStackCount: Int = 32,
    shape: Shape = RoundedCornerShape(size = 8.dp),
    barColors: List<Color> = listOf(
        Color.Red, Color.Yellow, Color.Green
    ),
    stackBarBackgroundColor: Color = Color.Gray
) {

    var size by remember { mutableStateOf(IntSize.Zero) }

    Row(modifier = modifier.onSizeChanged { size = it }) {

        val viewportWidth = size.width.toFloat()
        val viewportHeight = size.height.toFloat()
        val padding = LocalDensity.current.run { 1.dp.toPx() }

        val nodes = calculateStackedBarPoints(
            resampled = data.resample(barCount),
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
            barCount = barCount,
            maxStackCount = maxStackCount,
            horizontalPadding = padding,
            verticalPadding = padding
        ).mapIndexed { index, point ->
            if (index % 4 == 0) {
                PathNode.MoveTo(point.x(), point.y())
            } else {
                PathNode.LineTo(point.x(), point.y())
            }
        }

        val vectorPainter = rememberVectorPainter(
            defaultHeight = viewportHeight.dp,
            defaultWidth = viewportWidth.dp,
            viewportHeight = viewportHeight,
            viewportWidth = viewportWidth,
            autoMirror = false
        ) { _, _ ->
            Path(
                fill = Brush.linearGradient(
                    colors = barColors,
                    start = Offset.Zero, end = Offset(0f, Float.POSITIVE_INFINITY)
                ),
                pathData = nodes
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = shape)
        )
        {
            StackedBarVisualizerBackground(
                barCount = barCount,
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight,
                horizontalPadding = padding,
                verticalPadding = padding,
                stackBarBackgroundColor = stackBarBackgroundColor
            )
            Image(
                painter = vectorPainter,
                contentDescription = ""
            )
        }
    }
}

@Composable
private fun calculateStackedBarPoints(
    resampled: IntArray,
    viewportWidth: Float,
    viewportHeight: Float,
    barCount: Int,
    maxStackCount: Int,
    horizontalPadding: Float,
    verticalPadding: Float
): List<Point> {

    val barWidth = (viewportWidth / barCount) - horizontalPadding
    val stackHeightWithPadding = viewportHeight / maxStackCount
    val stackHeight = stackHeightWithPadding - verticalPadding

    val nodes = mutableListOf<Point>()

    resampled.forEachIndexed { index, d ->

        val stackCount = animateIntAsState(
            targetValue = (maxStackCount * (d / 128f)).roundToInt(),
            animationSpec = tween(durationMillis = VisualizerHelper.SAMPLING_INTERVAL)
        )

        for (stackIndex in 0..stackCount.value) {
            nodes += Point(
                barWidth * index + horizontalPadding * index to
                        viewportHeight - stackIndex * stackHeight - verticalPadding * stackIndex
            )
            nodes += Point(
                barWidth * (index + 1) + horizontalPadding * index to
                        viewportHeight - stackIndex * stackHeight - verticalPadding * stackIndex
            )
            nodes += Point(
                barWidth * (index + 1) + horizontalPadding * index to
                        viewportHeight - (stackIndex + 1) * stackHeight - verticalPadding * stackIndex
            )
            nodes += Point(
                barWidth * index + horizontalPadding * index to
                        viewportHeight - (stackIndex + 1) * stackHeight - verticalPadding * stackIndex
            )
        }
    }
    return nodes
}

@Composable
private fun StackedBarVisualizerBackground(
    barCount: Int,
    maxStackCount: Int = 32,
    stackBarBackgroundColor: Color = Color.Gray,
    viewportWidth: Float,
    viewportHeight: Float,
    horizontalPadding: Float,
    verticalPadding: Float
) {
    Row(modifier = Modifier.fillMaxSize()) {

        val nodes = calculateStackedBarPoints(
            resampled = VisualizerData.getMaxProcessed(resolution = barCount),
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
            barCount = barCount,
            maxStackCount = maxStackCount,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding
        ).mapIndexed { index, point ->
            if (index % 4 == 0) {
                PathNode.MoveTo(point.x(), point.y())
            } else {
                PathNode.LineTo(point.x(), point.y())
            }
        }

        val vectorPainter = rememberVectorPainter(
            defaultHeight = viewportHeight.dp,
            defaultWidth = viewportWidth.dp,
            viewportHeight = viewportHeight,
            viewportWidth = viewportWidth,
            autoMirror = false
        ) { _, _ ->
            Path(
                fill = SolidColor(stackBarBackgroundColor),
                pathData = nodes
            )
        }

        Image(
            painter = vectorPainter,
            contentDescription = ""
        )
    }
}