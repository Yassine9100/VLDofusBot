package fr.lewon.dofus.bot.scripts.tasks.impl.hunt

import fr.lewon.dofus.bot.core.logs.LogItem
import fr.lewon.dofus.bot.core.manager.DofusMapManager
import fr.lewon.dofus.bot.scripts.CancellationToken
import fr.lewon.dofus.bot.scripts.tasks.BooleanDofusBotTask
import fr.lewon.dofus.bot.scripts.tasks.impl.moves.TravelTask
import fr.lewon.dofus.bot.scripts.tasks.impl.transport.ReachMapTask
import fr.lewon.dofus.bot.util.game.TreasureHuntUtil
import fr.lewon.dofus.bot.util.geometry.PointRelative
import fr.lewon.dofus.bot.util.io.MouseUtil
import fr.lewon.dofus.bot.util.io.ScreenUtil
import fr.lewon.dofus.bot.util.io.WaitUtil
import fr.lewon.dofus.bot.util.network.GameInfo
import java.awt.Color

class FetchHuntTask : BooleanDofusBotTask() {

    companion object {
        private const val HUNT_MALL_OUTSIDE_MAP_ID = 142089230.0
        private const val HUNT_MALL_INSIDE_MAP_ID = 128452097.0
        private val HUNT_MALL_CHEST_POINT = PointRelative(0.55443037f, 0.44620255f)
        private val HUNT_SEEK_OPTION_POINT = PointRelative(0.56329113f, 0.4825949f)
        private val HUNT_SEEK_OPTION_MIN_COLOR = Color(0, 0, 0)
        private val HUNT_SEEK_OPTION_MAX_COLOR = Color(25, 25, 25)
    }

    override fun execute(logItem: LogItem, gameInfo: GameInfo, cancellationToken: CancellationToken): Boolean {
        gameInfo.treasureHunt = null
        if (TreasureHuntUtil.isHuntPresent(gameInfo)) {
            return true
        }
        val outsideMap = DofusMapManager.getDofusMap(HUNT_MALL_OUTSIDE_MAP_ID)
        val insideMap = DofusMapManager.getDofusMap(HUNT_MALL_INSIDE_MAP_ID)
        ReachMapTask(outsideMap).run(logItem, gameInfo, cancellationToken)
        TravelTask(listOf(insideMap)).run(logItem, gameInfo, cancellationToken)
        WaitUtil.sleep(2000)
        MouseUtil.leftClick(gameInfo, HUNT_MALL_CHEST_POINT, 0, false)
        if (!WaitUtil.waitUntil({ isHuntSeekOptionFound(gameInfo) }, cancellationToken)) {
            return false
        }
        MouseUtil.leftClick(gameInfo, HUNT_SEEK_OPTION_POINT)
        if (!WaitUtil.waitUntil({ TreasureHuntUtil.isHuntPresent(gameInfo) }, cancellationToken)) {
            return false
        }
        TravelTask(listOf(outsideMap)).run(logItem, gameInfo, cancellationToken)
        return true
    }

    private fun isHuntSeekOptionFound(gameInfo: GameInfo): Boolean {
        MouseUtil.move(gameInfo, HUNT_MALL_CHEST_POINT, 100)
        return ScreenUtil.isBetween(
            gameInfo,
            HUNT_SEEK_OPTION_POINT,
            HUNT_SEEK_OPTION_MIN_COLOR,
            HUNT_SEEK_OPTION_MAX_COLOR
        )
    }

    override fun onStarted(): String {
        return "Fetching new treasure hunt ..."
    }
}