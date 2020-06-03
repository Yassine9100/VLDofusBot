package fr.lewon.dofus.bot.ui.logic.tasks.complex

import fr.lewon.dofus.bot.ui.DofusTreasureBotGUIController
import fr.lewon.dofus.bot.ui.LogItem
import fr.lewon.dofus.bot.ui.logic.DofusBotTask
import fr.lewon.dofus.bot.ui.logic.tasks.ClickButtonTask
import fr.lewon.dofus.bot.util.Directions
import fr.lewon.dofus.bot.util.DofusImages
import fr.lewon.dofus.bot.util.GameInfoUtil
import javafx.concurrent.WorkerStateEvent

class MultimapRandomSearchTask(
    controller: DofusTreasureBotGUIController,
    parentLogItem: LogItem?,
    private val direction: Directions
) : DofusBotTask<Pair<Int, Int>>(controller, parentLogItem) {

    override fun execute(logItem: LogItem): Pair<Int, Int> {
        var objectsToFind = getObjectsToFind()
        val originalObjectsToFindSize = objectsToFind.size
        var pos: Pair<Int, Int>? = null
        while (objectsToFind.size == originalObjectsToFindSize) {
            pos = direction.buildMoveTask(controller, logItem).runAndGet()
            ClickButtonTask(controller, logItem, DofusImages.CHECKPOINT_BTN.path).runAndGet()
            Thread.sleep(800)
            ClickButtonTask(controller, logItem, DofusImages.SEARCH_BTN.path).runAndGet()
            Thread.sleep(800)
            objectsToFind = getObjectsToFind()
        }
        if (pos == null) {
            error("Invalid move")
        }
        return pos
    }

    private fun getObjectsToFind(): List<String> {
        val gameImage = controller.captureGameImage()
        val huntPanel = GameInfoUtil.getHuntPanel(gameImage)
            ?: throw Exception("Failed to retrieve hunt panel")
        return GameInfoUtil.getHuntObjectives(huntPanel)
    }

    override fun onFailed(event: WorkerStateEvent, logItem: LogItem) {
        controller.closeLog("KO - ${event.source.exception.localizedMessage}", logItem)
    }

    override fun onSucceeded(event: WorkerStateEvent, value: Pair<Int, Int>, logItem: LogItem) {
        controller.closeLog("OK : [${value.first},${value.second}]", logItem)
    }

    override fun onStarted(parentLogItem: LogItem?): LogItem {
        return controller.log("Moving in the [$direction] direction in hope to find hint ...", parentLogItem)
    }

}