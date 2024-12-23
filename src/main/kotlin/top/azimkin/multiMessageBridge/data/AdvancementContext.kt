package top.azimkin.multiMessageBridge.data

import io.papermc.paper.advancement.AdvancementDisplay

data class AdvancementContext(
    val playerName: String,
    val advancementName: String,
    val description: String,
    val rarity: AdvancementDisplay.Frame,
)
