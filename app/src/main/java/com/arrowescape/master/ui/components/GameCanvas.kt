package com.arrowescape.master.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.arrowescape.master.model.FlyingArrow
import com.arrowescape.master.model.GameState
import com.arrowescape.master.model.GridTile
import com.arrowescape.master.model.ThemeColors
import kotlin.math.sin

@Composable
fun GameCanvas(
    gameState: GameState,
    particles: List<Particle>,
    onTileTapped: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = gameState.themeColors
    val reusableArrowPath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(gameState.cols.toFloat() / gameState.rows.toFloat())
                .pointerInput(gameState.rows, gameState.cols, gameState.status) {
                    detectTapGestures { offset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val cellWidth = width / gameState.cols
                        val cellHeight = height / gameState.rows

                        val col = (offset.x / cellWidth).toInt().coerceIn(0, gameState.cols - 1)
                        val row = (offset.y / cellHeight).toInt().coerceIn(0, gameState.rows - 1)

                        onTileTapped(row, col)
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val cellWidth = canvasWidth / gameState.cols
            val cellHeight = canvasHeight / gameState.rows
            val tilePadding = cellWidth * 0.08f

            // 1. Draw Board Grid Container
            drawBoardGridSlots(
                rows = gameState.rows,
                cols = gameState.cols,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                padding = tilePadding,
                theme = theme
            )

            // 2. Draw Grid Tiles (Rocks, Static Arrows, Rotators, Shields)
            for (r in 0 until gameState.rows) {
                for (c in 0 until gameState.cols) {
                    val tile = gameState.grid.getOrNull(r)?.getOrNull(c) ?: GridTile.Empty
                    val isHinted = (tile is GridTile.ArrowTile && tile.id == gameState.hintArrowId)

                    drawTile(
                        tile = tile,
                        row = r,
                        col = c,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        padding = tilePadding,
                        theme = theme,
                        isHinted = isHinted,
                        arrowPath = reusableArrowPath
                    )
                }
            }

            // 3. Draw Actively Flying Escaping Arrows (Super Fluid 120 FPS Layer)
            for (flying in gameState.flyingArrows) {
                drawFlyingArrow(
                    flying = flying,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    padding = tilePadding,
                    theme = theme,
                    arrowPath = reusableArrowPath
                )
            }

            // 4. Draw Particle System FX
            drawParticleSystem(
                particles = particles,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                boardLeft = 0f,
                boardTop = 0f
            )
        }
    }
}

private fun DrawScope.drawBoardGridSlots(
    rows: Int,
    cols: Int,
    cellWidth: Float,
    cellHeight: Float,
    padding: Float,
    theme: ThemeColors
) {
    drawRoundRect(
        color = theme.boardBackground,
        cornerRadius = CornerRadius(24f, 24f),
        size = size
    )

    drawRoundRect(
        color = theme.gridLine,
        cornerRadius = CornerRadius(24f, 24f),
        size = size,
        style = Stroke(width = 3f)
    )

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val left = c * cellWidth + padding
            val top = r * cellHeight + padding
            val tileWidth = cellWidth - padding * 2
            val tileHeight = cellHeight - padding * 2

            drawRoundRect(
                color = Color(0x1A000000),
                topLeft = Offset(left, top),
                size = Size(tileWidth, tileHeight),
                cornerRadius = CornerRadius(16f, 16f)
            )

            drawRoundRect(
                color = theme.gridLine.copy(alpha = 0.15f),
                topLeft = Offset(left, top),
                size = Size(tileWidth, tileHeight),
                cornerRadius = CornerRadius(16f, 16f),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

private fun DrawScope.drawTile(
    tile: GridTile,
    row: Int,
    col: Int,
    cellWidth: Float,
    cellHeight: Float,
    padding: Float,
    theme: ThemeColors,
    isHinted: Boolean,
    arrowPath: Path
) {
    if (tile is GridTile.Empty) return

    var left = col * cellWidth + padding
    var top = row * cellHeight + padding
    val tileWidth = cellWidth - padding * 2
    val tileHeight = cellHeight - padding * 2

    when (tile) {
        is GridTile.Rock -> {
            left += tile.shakeOffset
            drawRockTile(left, top, tileWidth, tileHeight, theme)
        }
        is GridTile.ArrowTile -> {
            left += tile.shakeOffset
            top += 0f

            drawArrowTileContent(
                left = left,
                top = top,
                width = tileWidth,
                height = tileHeight,
                direction = tile.direction,
                isRotator = tile.isRotator,
                shieldCount = tile.shieldCount,
                alpha = tile.alpha,
                scale = tile.scale,
                rotationAnimDegrees = tile.rotationAnimDegrees,
                theme = theme,
                isHinted = isHinted,
                arrowPath = arrowPath
            )
        }
        else -> {}
    }
}

private fun DrawScope.drawFlyingArrow(
    flying: FlyingArrow,
    cellWidth: Float,
    cellHeight: Float,
    padding: Float,
    theme: ThemeColors,
    arrowPath: Path
) {
    val leftBase = flying.col * cellWidth + padding
    val topBase = flying.row * cellHeight + padding
    val tileWidth = cellWidth - padding * 2
    val tileHeight = cellHeight - padding * 2

    val dr = flying.direction.dr.toFloat()
    val dc = flying.direction.dc.toFloat()
    val ease = sin(flying.progress * (Math.PI / 2.0)).toFloat()

    val left = leftBase + dc * ease * 4.5f * cellWidth
    val top = topBase + dr * ease * 4.5f * cellHeight
    val alpha = (1f - flying.progress * flying.progress).coerceIn(0f, 1f)
    val scaleVal = 1f + flying.progress * 0.2f

    drawArrowTileContent(
        left = left,
        top = top,
        width = tileWidth,
        height = tileHeight,
        direction = flying.direction,
        isRotator = flying.isRotator,
        shieldCount = flying.shieldCount,
        alpha = alpha,
        scale = scaleVal,
        rotationAnimDegrees = 0f,
        theme = theme,
        isHinted = false,
        arrowPath = arrowPath
    )
}

private fun DrawScope.drawRockTile(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    theme: ThemeColors
) {
    val corner = CornerRadius(16f, 16f)

    drawRoundRect(
        color = theme.rockFill,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = corner
    )

    drawRoundRect(
        color = theme.rockBorder,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = corner,
        style = Stroke(width = 3f)
    )

    drawLine(
        color = theme.rockBorder.copy(alpha = 0.5f),
        start = Offset(left + width * 0.25f, top + height * 0.25f),
        end = Offset(left + width * 0.75f, top + height * 0.75f),
        strokeWidth = 3f
    )
    drawLine(
        color = theme.rockBorder.copy(alpha = 0.5f),
        start = Offset(left + width * 0.75f, top + height * 0.25f),
        end = Offset(left + width * 0.25f, top + height * 0.75f),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawArrowTileContent(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    direction: com.arrowescape.master.model.Direction,
    isRotator: Boolean,
    shieldCount: Int,
    alpha: Float,
    scale: Float,
    rotationAnimDegrees: Float,
    theme: ThemeColors,
    isHinted: Boolean,
    arrowPath: Path
) {
    if (alpha <= 0.01f) return

    val corner = CornerRadius(16f, 16f)
    val centerX = left + width / 2f
    val centerY = top + height / 2f

    scale(scale = scale, pivot = Offset(centerX, centerY)) {

        if (isHinted) {
            val pulse = sin(System.currentTimeMillis() * 0.008f) * 4f + 8f
            drawRoundRect(
                color = theme.accent.copy(alpha = 0.4f * alpha),
                topLeft = Offset(left - pulse, top - pulse),
                size = Size(width + pulse * 2f, height + pulse * 2f),
                cornerRadius = CornerRadius(22f, 22f)
            )
            drawRoundRect(
                color = theme.accent.copy(alpha = alpha),
                topLeft = Offset(left - pulse, top - pulse),
                size = Size(width + pulse * 2f, height + pulse * 2f),
                cornerRadius = CornerRadius(22f, 22f),
                style = Stroke(width = 4f)
            )
        }

        val tileBrush = Brush.linearGradient(
            colors = listOf(
                theme.arrowPrimary.copy(alpha = alpha),
                theme.arrowSecondary.copy(alpha = alpha)
            ),
            start = Offset(left, top),
            end = Offset(left + width, top + height)
        )

        drawRoundRect(
            brush = tileBrush,
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = corner
        )

        drawRoundRect(
            color = theme.arrowGlow.copy(alpha = 0.8f * alpha),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = corner,
            style = Stroke(width = 2.5f)
        )

        val totalRotationDegrees = direction.degrees + rotationAnimDegrees
        rotate(degrees = totalRotationDegrees, pivot = Offset(centerX, centerY)) {
            arrowPath.reset()
            val headWidth = width * 0.40f
            val shaftWidth = width * 0.18f

            arrowPath.moveTo(centerX, centerY - height * 0.32f)
            arrowPath.lineTo(centerX + headWidth / 2f, centerY - height * 0.02f)
            arrowPath.lineTo(centerX + shaftWidth / 2f, centerY - height * 0.02f)
            arrowPath.lineTo(centerX + shaftWidth / 2f, centerY + height * 0.30f)
            arrowPath.lineTo(centerX - shaftWidth / 2f, centerY + height * 0.30f)
            arrowPath.lineTo(centerX - shaftWidth / 2f, centerY - height * 0.02f)
            arrowPath.lineTo(centerX - headWidth / 2f, centerY - height * 0.02f)
            arrowPath.close()

            drawPath(
                path = arrowPath,
                color = Color.White.copy(alpha = alpha)
            )
        }

        if (isRotator) {
            drawCircle(
                color = theme.rotatorPivot.copy(alpha = alpha),
                radius = width * 0.18f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = width * 0.18f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3f)
            )
        }

        if (shieldCount > 0) {
            drawCircle(
                color = theme.shieldArmor.copy(alpha = 0.85f * alpha),
                radius = width * 0.42f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = width * 0.42f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
            )

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = width * 0.35f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    this.alpha = (255 * alpha).toInt()
                }
                drawText(
                    shieldCount.toString(),
                    centerX,
                    centerY + (width * 0.12f),
                    paint
                )
            }
        }
    }
}
