package fr.lewon.dofus.bot.handlers

import fr.lewon.dofus.bot.core.logs.VldbLogger
import fr.lewon.dofus.bot.game.GameInfo
import fr.lewon.dofus.bot.sniffer.model.messages.fight.GameFightShowFighterMessage
import fr.lewon.dofus.bot.sniffer.store.EventHandler

object GameFightShowFighterEventHandler : EventHandler<GameFightShowFighterMessage> {

    override fun onEventReceived(socketResult: GameFightShowFighterMessage) {
        val fighterId = socketResult.informations.contextualId
        val cellId = socketResult.informations.spawnInfo.informations.disposition.cellId
        val characteristics = socketResult.informations.stats.characteristics.characteristics
        GameInfo.fightBoard.createOrUpdateFighter(fighterId, cellId)
        GameInfo.fightBoard.updateFighterCharacteristics(fighterId, characteristics)
        VldbLogger.debug("Fighter [$fighterId] characteristics and position updated")
    }

}