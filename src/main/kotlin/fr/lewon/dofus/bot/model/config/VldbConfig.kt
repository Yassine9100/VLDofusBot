package fr.lewon.dofus.bot.model.config

import com.fasterxml.jackson.annotation.JsonProperty

data class VldbConfig(
    @field:JsonProperty var displayOverlays: Boolean = true,
    @field:JsonProperty var networkInterfaceName: String? = null,
    @field:JsonProperty var playSpecialMonsterSound: Boolean = true
)