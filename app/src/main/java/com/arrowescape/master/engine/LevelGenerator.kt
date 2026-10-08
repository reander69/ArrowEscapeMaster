package com.arrowescape.master.engine

import com.arrowescape.master.model.Direction
import com.arrowescape.master.model.GridTile
import java.util.UUID
import kotlin.random.Random

object LevelGenerator {

    data class GridSize(val rows: Int, val cols: Int)

    fun getGridSizeForLevel(level: Int): GridSize {
        return when {
            level <= 2 -> GridSize(3, 3)
            level <= 5 -> GridSize(4, 4)
            level <= 12 -> GridSize(5, 5)
            level <= 25 -> GridSize(5, 6)
            level <= 50 -> GridSize(6, 6)
            else -> GridSize(7, 8)
        }
    }

    fun generateLevel(levelNum: Int): List<List<GridTile>> {
        val size = getGridSizeForLevel(levelNum)
        val rows = size.rows
        val cols = size.cols

        val rockCount = when {
            levelNum < 5 -> 0
            levelNum < 12 -> 1
            levelNum < 25 -> 2
            levelNum < 50 -> 3
            else -> 4
        }

        val allowRotators = levelNum >= 12
        val allowShields = levelNum >= 25

        // Attempt reverse generation with fallback to ensure 100% guaranteed solvability
        for (attempt in 0 until 50) {
            val grid = buildCandidateGrid(
                rows = rows,
                cols = cols,
                rockCount = rockCount,
                allowRotators = allowRotators,
                allowShields = allowShields,
                seed = (levelNum * 1000 + attempt).toLong()
            )

            if (PuzzleSolver.isSolvable(grid, rows, cols)) {
                return grid
            }
        }

        // Fallback: guaranteed simple solvable grid if random iterations fail
        return buildGuaranteedFallbackGrid(rows, cols)
    }

    private fun buildCandidateGrid(
        rows: Int,
        cols: Int,
        rockCount: Int,
        allowRotators: Boolean,
        allowShields: Boolean,
        seed: Long
    ): List<List<GridTile>> {
        val random = Random(seed)
        val gridMatrix = Array(rows) { Array<GridTile>(cols) { GridTile.Empty } }

        // 1. Place Rocks randomly (avoiding covering every edge)
        var placedRocks = 0
        val maxRocks = rockCount.coerceAtMost((rows * cols) / 6)
        while (placedRocks < maxRocks) {
            val r = random.nextInt(1, rows - 1)
            val c = random.nextInt(1, cols - 1)
            if (gridMatrix[r][c] is GridTile.Empty) {
                gridMatrix[r][c] = GridTile.Rock()
                placedRocks++
            }
        }

        // 2. Fill empty cells with arrows using reverse solvability placement
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (gridMatrix[r][c] is GridTile.Empty) {
                    emptyCells.add(Pair(r, c))
                }
            }
        }
        emptyCells.shuffle(random)

        val totalArrows = (emptyCells.size * 0.85).toInt().coerceAtLeast(3)
        val placedArrows = emptyCells.take(totalArrows)

        for (cell in placedArrows) {
            val r = cell.first
            val c = cell.second

            // Find valid escape directions from this cell
            val validDirs = Direction.entries.filter { dir ->
                isPathToBorderClearOfRocks(gridMatrix, r, c, dir, rows, cols)
            }

            val chosenDir = if (validDirs.isNotEmpty()) {
                validDirs.random(random)
            } else {
                Direction.entries.random(random)
            }

            val isRotator = allowRotators && random.nextFloat() < 0.25f
            val shieldCount = if (allowShields && random.nextFloat() < 0.20f) {
                if (random.nextBoolean()) 1 else 2
            } else 0

            gridMatrix[r][c] = GridTile.ArrowTile(
                id = UUID.randomUUID().toString(),
                row = r,
                col = c,
                direction = chosenDir,
                isRotator = isRotator,
                shieldCount = shieldCount,
                initialShieldCount = shieldCount
            )
        }

        return gridMatrix.map { row -> row.toList() }
    }

    private fun isPathToBorderClearOfRocks(
        grid: Array<Array<GridTile>>,
        startR: Int,
        startC: Int,
        dir: Direction,
        rows: Int,
        cols: Int
    ): Boolean {
        var r = startR + dir.dr
        var c = startC + dir.dc
        while (r in 0 until rows && c in 0 until cols) {
            if (grid[r][c] is GridTile.Rock) {
                return false
            }
            r += dir.dr
            c += dir.dc
        }
        return true
    }

    private fun buildGuaranteedFallbackGrid(
        rows: Int,
        cols: Int
    ): List<List<GridTile>> {
        val gridMatrix = Array(rows) { Array<GridTile>(cols) { GridTile.Empty } }
        // Place outward-pointing arrows along edges and center
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val dir = when {
                    r == 0 -> Direction.UP
                    r == rows - 1 -> Direction.DOWN
                    c == 0 -> Direction.LEFT
                    c == cols - 1 -> Direction.RIGHT
                    r < rows / 2 -> Direction.UP
                    else -> Direction.DOWN
                }

                gridMatrix[r][c] = GridTile.ArrowTile(
                    id = UUID.randomUUID().toString(),
                    row = r,
                    col = c,
                    direction = dir,
                    isRotator = false,
                    shieldCount = 0
                )
            }
        }
        return gridMatrix.map { row -> row.toList() }
    }
}
