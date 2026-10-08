package com.arrowescape.master.model

import java.util.UUID

sealed class GridTile {
    abstract val id: String

    data object Empty : GridTile() {
        override val id: String = "empty"
    }

    data class Rock(
        override val id: String = UUID.randomUUID().toString(),
        val shakeOffset: Float = 0f
    ) : GridTile()

    data class ArrowTile(
        override val id: String = UUID.randomUUID().toString(),
        val row: Int,
        val col: Int,
        val direction: Direction,
        val isRotator: Boolean = false,
        val shieldCount: Int = 0,
        val initialShieldCount: Int = 0,
        // Fluid Animation States
        val offsetX: Float = 0f,
        val offsetY: Float = 0f,
        val alpha: Float = 1f,
        val scale: Float = 1f,
        val rotationAnimDegrees: Float = 0f,
        val shakeOffset: Float = 0f,
        val isShaking: Boolean = false,
        val isHighlighted: Boolean = false
    ) : GridTile() {
        val isShielded: Boolean get() = shieldCount > 0

        fun copyWithNewDirection(newDirection: Direction): ArrowTile {
            return this.copy(direction = newDirection)
        }

        fun copyWithReducedShield(): ArrowTile {
            return if (shieldCount > 0) this.copy(shieldCount = shieldCount - 1) else this
        }
    }
}
