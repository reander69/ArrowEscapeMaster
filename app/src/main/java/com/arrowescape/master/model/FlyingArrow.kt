package com.arrowescape.master.model

data class FlyingArrow(
    val id: String,
    val row: Int,
    val col: Int,
    val direction: Direction,
    val isRotator: Boolean,
    val shieldCount: Int,
    val progress: Float = 0f, // 0.0 to 1.0
    val alpha: Float = 1f,
    val scale: Float = 1f
)
