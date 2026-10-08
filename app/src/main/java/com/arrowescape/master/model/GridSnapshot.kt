package com.arrowescape.master.model

data class GridSnapshot(
    val grid: List<List<GridTile>>,
    val moveCount: Int
)
