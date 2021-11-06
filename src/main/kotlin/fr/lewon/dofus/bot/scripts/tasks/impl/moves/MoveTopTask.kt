package fr.lewon.dofus.bot.scripts.tasks.impl.moves

import fr.lewon.dofus.bot.core.manager.d2p.maps.cell.CellData
import fr.lewon.dofus.bot.core.model.move.Direction
import fr.lewon.dofus.bot.game.fight.FightBoard

class MoveTopTask : MoveTask(Direction.TOP) {

    override fun getOverrideX(): Float? {
        return null
    }

    override fun getOverrideY(): Float {
        return 0.0054744524f
    }

    override fun isCellOk(cellData: CellData): Boolean {
        val mapChangeData = cellData.mapChangeData
        return cellData.cellId < FightBoard.MAP_WIDTH * 2 &&
            (mapChangeData and 32 != 0 || mapChangeData and 64 != 0 || mapChangeData and 128 != 0)
    }

}