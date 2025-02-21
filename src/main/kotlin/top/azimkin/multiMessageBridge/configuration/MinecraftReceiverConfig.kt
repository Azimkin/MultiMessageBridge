package top.azimkin.multiMessageBridge.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import io.papermc.paper.advancement.AdvancementDisplay

// simplified
data class MinecraftReceiverConfig(
    @Comment("If enabled will filter advancements by rarity")
    var filterAdvancements: Boolean = false,
    private var enabledAdvancementRarity: List<String> = AdvancementDisplay.Frame.entries.map { it.name },
    var messages: MinecraftMessageList = MinecraftMessageList(),
) : OkaeriConfig() {
    fun enabledAdvancementRarity(): List<AdvancementDisplay.Frame> {
        return enabledAdvancementRarity.map { AdvancementDisplay.Frame.valueOf(it.uppercase()) }
    }
}

// tiny copy of MessageList
data class MinecraftMessageList(
    var messageBase: String = "<platform> <nickname> -> <message>",
    var customFormats: Map<String, String> = emptyMap(),
) : OkaeriConfig()

