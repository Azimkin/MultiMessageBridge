package top.azimkin.multiMessageBridge.data

import io.papermc.paper.advancement.AdvancementDisplay

data class AdvancementContext(
    val name: String,
    val description: String,
    val rarity: AdvancementDisplay.Frame,
)
