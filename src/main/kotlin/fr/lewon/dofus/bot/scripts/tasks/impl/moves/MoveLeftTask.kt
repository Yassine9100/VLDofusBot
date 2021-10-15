package fr.lewon.dofus.bot.scripts.tasks.impl.moves

import fr.lewon.dofus.bot.core.model.move.Direction
import fr.lewon.dofus.bot.util.filemanagers.ConfigManager
import fr.lewon.dofus.bot.util.geometry.PointRelative

class MoveLeftTask : MoveTask(Direction.LEFT) {

    override fun getMoveDest(): PointRelative {
        return ConfigManager.config.leftAccessPos
    }

}