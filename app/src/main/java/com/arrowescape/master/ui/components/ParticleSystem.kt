package com.arrowescape.master.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

enum class ParticleType {
    SPARK,
    SHOCKWAVE_RING,
    GLOW_DOT
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var age: Int = 0,
    var maxAge: Int = 20,
    var size: Float = 6f,
    var type: ParticleType = ParticleType.SPARK
) {
    val progress: Float get() = (age.toFloat() / maxAge.toFloat()).coerceIn(0f, 1f)
    val alpha: Float get() = (1f - progress).coerceIn(0f, 1f)
}

fun DrawScope.drawParticleSystem(
    particles: List<Particle>,
    cellWidth: Float,
    cellHeight: Float,
    boardLeft: Float,
    boardTop: Float
) {
    for (p in particles) {
        val px = boardLeft + p.x * cellWidth
        val py = boardTop + p.y * cellHeight

        when (p.type) {
            ParticleType.SPARK -> {
                val currentRadius = (1f - p.progress) * p.size + 2f
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = currentRadius,
                    center = Offset(px, py)
                )
            }
            ParticleType.SHOCKWAVE_RING -> {
                val currentRadius = p.progress * p.size * (cellWidth * 0.8f)
                drawCircle(
                    color = p.color.copy(alpha = p.alpha * 0.7f),
                    radius = currentRadius,
                    center = Offset(px, py),
                    style = Stroke(width = (1f - p.progress) * 8f + 1.5f)
                )
            }
            ParticleType.GLOW_DOT -> {
                val currentRadius = (1f - p.progress * 0.5f) * p.size
                drawCircle(
                    color = p.color.copy(alpha = p.alpha * 0.5f),
                    radius = currentRadius * 1.8f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White.copy(alpha = p.alpha),
                    radius = currentRadius * 0.8f,
                    center = Offset(px, py)
                )
            }
        }
    }
}
