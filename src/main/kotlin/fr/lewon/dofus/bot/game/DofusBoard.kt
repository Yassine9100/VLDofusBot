package fr.lewon.dofus.bot.game

import fr.lewon.dofus.bot.core.manager.d2p.maps.D2PMapsAdapter
import fr.lewon.dofus.bot.core.manager.d2p.maps.cell.CellData
import fr.lewon.dofus.bot.util.geometry.RectangleRelative
import java.util.*

class DofusBoard {

    companion object {
        private const val HEIGHT_RATIO = 0.885f
        const val MAP_CELLS_COUNT = D2PMapsAdapter.MAP_CELLS_COUNT
        const val MAP_WIDTH = 14
        const val MAP_HEIGHT = 20
        const val TILE_WIDTH = 1f / (MAP_WIDTH.toFloat() + 0.5f)
        const val TILE_HEIGHT = HEIGHT_RATIO / (MAP_HEIGHT.toFloat() + 0.5f)
    }

    var startCells: List<DofusCell> = ArrayList()
    val cells: ArrayList<DofusCell>
    private val cellsByPosition = HashMap<Pair<Int, Int>, DofusCell>()
    private val cellsByCellId = HashMap<Int, DofusCell>()

    init {
        val initialX = TILE_WIDTH / 2f
        val initialY = TILE_HEIGHT / 4f

        cells = ArrayList()
        for (xMultiplier in 0 until MAP_WIDTH * 2) {
            val x = initialX + (xMultiplier.toFloat() / 2f) * TILE_WIDTH
            for (yMultiplier in 0 until MAP_HEIGHT) {
                val row = -xMultiplier / 2 + yMultiplier
                val col = xMultiplier / 2 + xMultiplier % 2 + yMultiplier
                val cellId = xMultiplier / 2 + yMultiplier * 2 * MAP_WIDTH + xMultiplier % 2 * MAP_WIDTH
                val y = initialY + yMultiplier * TILE_HEIGHT + (xMultiplier % 2) * TILE_HEIGHT / 2f
                val boundsX = x - TILE_WIDTH / 4f
                val boundsW = TILE_WIDTH / 2f
                val boundsH = TILE_HEIGHT / 2f
                val bounds = RectangleRelative(boundsX, y, boundsW, boundsH)
                cells.add(DofusCell(row, col, cellId, bounds))
            }
        }
        initNeighbors(cells)
    }

    private fun initNeighbors(cells: List<DofusCell>) {
        for (cell in cells) {
            cellsByPosition[Pair(cell.col, cell.row)] = cell
            cellsByCellId[cell.cellId] = cell
        }
        for (cell in cells) {
            val neighbors = listOfNotNull(
                cellsByPosition[Pair(cell.col - 1, cell.row)],
                cellsByPosition[Pair(cell.col + 1, cell.row)],
                cellsByPosition[Pair(cell.col, cell.row - 1)],
                cellsByPosition[Pair(cell.col, cell.row + 1)]
            )
            cell.neighbors.addAll(neighbors)
        }
    }

    fun isOnSameLine(fromCellId: Int, toCellId: Int): Boolean {
        val from = getCell(fromCellId)
        val to = getCell(toCellId)
        return from.row == to.row || from.col == to.col
    }

    fun getCell(cellId: Int): DofusCell {
        return cellsByCellId[cellId] ?: error("No cell with id $cellId")
    }

    fun getCell(col: Int, row: Int): DofusCell? {
        return cellsByPosition[Pair(col, row)]
    }

    fun updateCells(cellDataList: List<CellData>) {
        for (cellData in cellDataList) {
            val cell = getCell(cellData.cellId)
            cell.cellData = cellData
        }
    }

    fun updateStartCells(positionsForChallengers: ArrayList<Int>) {
        startCells = positionsForChallengers.map { getCell(it) }
    }

    fun getPathLength(fromCell: DofusCell, toCell: DofusCell): Int? {
        return getPath(fromCell, toCell)?.size
    }

    fun getDist(fromCell: DofusCell, toCell: DofusCell): Int? {
        return getPath(fromCell, toCell, true)?.size
    }

    private fun getPath(fromCell: DofusCell, toCell: DofusCell, fly: Boolean = false): List<DofusCell>? {
        if (fromCell == toCell) {
            return emptyList()
        }
        var node = findPath(fromCell, toCell, fly) ?: return null
        val cells = LinkedList<DofusCell>()
        while (node.parent != null) {
            cells.push(node.cell)
            node = node.parent!!
        }
        return cells
    }

    private fun findPath(fromCell: DofusCell, toCell: DofusCell, fly: Boolean = false): Node? {
        val initialNode = Node(null, fromCell)
        val explored = mutableListOf(fromCell)
        var frontier = listOf(initialNode)
        while (frontier.isNotEmpty()) {
            val newFrontier = ArrayList<Node>()
            for (node in frontier) {
                if (node.cell == toCell) {
                    return node
                }
                for (neighbor in node.cell.neighbors) {
                    if (!explored.contains(neighbor) && (fly || neighbor.isAccessible())) {
                        explored.add(neighbor)
                        newFrontier.add(Node(node, neighbor))
                    }
                }
            }
            frontier = newFrontier
        }
        return null
    }

    fun cellsAtRange(minRange: Int, maxRange: Int, fromCellId: Int): List<DofusCell> {
        val fromCell = getCell(fromCellId)
        return cellsAtRange(minRange, maxRange, fromCell)
    }

    fun cellsAtRange(minRange: Int, maxRange: Int, fromCell: DofusCell): List<DofusCell> {
        val cellsAtRange = ArrayList<DofusCell>()
        val explored = mutableListOf(fromCell)
        var frontier = listOf(fromCell)

        if (minRange == 0) {
            cellsAtRange.add(fromCell)
        }

        for (i in 1..maxRange) {
            val newFrontier = ArrayList<DofusCell>()
            for (cell in frontier) {
                for (neighbor in cell.neighbors) {
                    if (!explored.contains(neighbor)) {
                        if (i >= minRange) {
                            cellsAtRange.add(neighbor)
                        }
                        explored.add(neighbor)
                        newFrontier.add(neighbor)
                    }
                }
            }
            frontier = newFrontier
        }
        return cellsAtRange
    }

    private class Node(val parent: Node?, val cell: DofusCell)

}