package com.arrowescape.master.engine

import com.arrowescape.master.model.Direction
import com.arrowescape.master.model.GridTile

object PuzzleSolver {

    /**
     * Checks whether the path for an arrow in its pointing direction to the board edge is clear.
     */
    fun canArrowEscape(
        arrow: GridTile.ArrowTile,
        grid: List<List<GridTile>>,
        rows: Int,
        cols: Int
    ): Boolean {
        if (arrow.shieldCount > 0) return false

        var currRow = arrow.row + arrow.direction.dr
        var currCol = arrow.col + arrow.direction.dc

        while (currRow in 0 until rows && currCol in 0 until cols) {
            val tile = grid[currRow][currCol]
            if (tile !is GridTile.Empty) {
                return false
            }
            currRow += arrow.direction.dr
            currCol += arrow.direction.dc
        }
        return true
    }

    /**
     * Intelligent Hint finder:
     * 1. Finds an arrow that can escape immediately.
     * 2. If none, finds a shielded arrow whose armor needs to be cracked.
     * 3. If none, finds a rotator arrow that can be rotated.
     */
    fun findNextHintTile(
        grid: List<List<GridTile>>,
        rows: Int,
        cols: Int
    ): GridTile.ArrowTile? {
        // 1. Check for free escaping arrows
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val tile = grid[r][c]
                if (tile is GridTile.ArrowTile) {
                    if (canArrowEscape(tile, grid, rows, cols)) {
                        return tile
                    }
                }
            }
        }

        // 2. Check for shielded arrows that need armor cracking
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val tile = grid[r][c]
                if (tile is GridTile.ArrowTile && tile.shieldCount > 0) {
                    return tile
                }
            }
        }

        // 3. Check for rotator arrows
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val tile = grid[r][c]
                if (tile is GridTile.ArrowTile && tile.isRotator) {
                    var rotDir = tile.direction.rotateClockwise()
                    for (i in 0 until 3) {
                        val testTile = tile.copyWithNewDirection(rotDir)
                        if (canArrowEscape(testTile, grid, rows, cols)) {
                            return tile
                        }
                        rotDir = rotDir.rotateClockwise()
                    }
                }
            }
        }

        return null
    }

    /**
     * Solvability verification using Breadth-First Search / Greedy simulation.
     * Guarantees 100% solvability without infinite loops.
     */
    fun isSolvable(
        initialGrid: List<List<GridTile>>,
        rows: Int,
        cols: Int
    ): Boolean {
        val mutableGrid = initialGrid.map { row -> row.toMutableList() }.toMutableList()
        var madeProgress = true

        while (madeProgress) {
            madeProgress = false

            // 1. Check if any unshielded arrow can escape immediately
            var freeArrow: GridTile.ArrowTile? = null
            outer@ for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val tile = mutableGrid[r][c]
                    if (tile is GridTile.ArrowTile && canArrowEscape(tile, mutableGrid, rows, cols)) {
                        freeArrow = tile
                        break@outer
                    }
                }
            }

            if (freeArrow != null) {
                mutableGrid[freeArrow.row][freeArrow.col] = GridTile.Empty
                madeProgress = true
                continue
            }

            // 2. Try interacting with Shields or Rotators
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val tile = mutableGrid[r][c]
                    if (tile is GridTile.ArrowTile) {
                        if (tile.shieldCount > 0) {
                            mutableGrid[r][c] = tile.copyWithReducedShield()
                            madeProgress = true
                            break
                        } else if (tile.isRotator) {
                            var rotDirection = tile.direction.rotateClockwise()
                            for (rot in 0 until 3) {
                                val testTile = tile.copyWithNewDirection(rotDirection)
                                if (canArrowEscape(testTile, mutableGrid, rows, cols)) {
                                    mutableGrid[r][c] = GridTile.Empty
                                    madeProgress = true
                                    break
                                }
                                rotDirection = rotDirection.rotateClockwise()
                            }
                            if (madeProgress) break
                        }
                    }
                }
                if (madeProgress) break
            }
        }

        val remainingArrows = mutableGrid.sumOf { row ->
            row.count { it is GridTile.ArrowTile }
        }
        return remainingArrows == 0
    }
}
