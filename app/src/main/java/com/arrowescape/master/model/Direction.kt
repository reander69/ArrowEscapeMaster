package com.arrowescape.master.model

enum class Direction(val dr: Int, val dc: Int, val degrees: Float) {
    UP(-1, 0, 0f),
    RIGHT(0, 1, 90f),
    DOWN(1, 0, 180f),
    LEFT(0, -1, 270f);

    fun rotateClockwise(): Direction {
        return when (this) {
            UP -> RIGHT
            RIGHT -> DOWN
            DOWN -> LEFT
            LEFT -> UP
        }
    }

    companion object {
        fun random(): Direction {
            return entries.random()
        }
    }
}
