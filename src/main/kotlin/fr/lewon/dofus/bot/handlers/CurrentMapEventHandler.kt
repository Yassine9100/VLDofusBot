package fr.lewon.dofus.bot.handlers

import fr.lewon.dofus.bot.core.manager.d2p.maps.D2PMapsAdapter
import fr.lewon.dofus.bot.sniffer.model.messages.move.CurrentMapMessage
import fr.lewon.dofus.bot.sniffer.store.EventHandler
import fr.lewon.dofus.bot.util.network.GameSnifferUtil

object CurrentMapEventHandler : EventHandler<CurrentMapMessage> {
    override fun onEventReceived(socketResult: CurrentMapMessage, snifferId: Long) {
        val gameInfo = GameSnifferUtil.getGameInfoBySnifferId(snifferId)
        val completeCellDataByCellId =
            D2PMapsAdapter.getCompleteCellDataByCellId(socketResult.map.id, socketResult.mapKey)
        gameInfo.dofusBoard.updateCells(completeCellDataByCellId.values.map { it.cellData })
        gameInfo.completeCellDataByCellId = completeCellDataByCellId
    }
}