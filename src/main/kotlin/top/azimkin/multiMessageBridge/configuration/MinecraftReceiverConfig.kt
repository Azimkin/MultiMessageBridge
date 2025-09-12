package top.azimkin.multiMessageBridge.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import io.papermc.paper.advancement.AdvancementDisplay

// simplified
data class MinecraftReceiverConfig(
    var dispatchAdvancements: Boolean = false,
    @Comment("If enabled will filter advancements by rarity")
    var filterAdvancements: Boolean = true,
    private var enabledAdvancementRarity: List<String> = AdvancementDisplay.Frame.entries.filter { it.name != "TASK" }
        .map { it.name },
    var messages: MinecraftMessageList = MinecraftMessageList(),
    var chatHandlerConfiguration: Map<String, String> = emptyMap()
) : OkaeriConfig() {
    fun enabledAdvancementRarity(): List<AdvancementDisplay.Frame> {
        return enabledAdvancementRarity.map { AdvancementDisplay.Frame.valueOf(it.uppercase()) }
    }
}

// tiny copy of MessageList
data class MinecraftMessageList(
    @Comment("In 0.5 replies was introduced.")
    @Comment("    Use <reply> tag to insert a reply part")
    @Comment("    Use <sticker> tag to insert a sticker part")
    @Comment("    Use <attachment> tag to insert an attachment part")
    @Comment("If you use customFormats it also should be specified")
    var messageBase: String = "<platform> <reply><nickname> -> <message> <sticker><attachments>",
    var reply: String = "<blue><hover:show_text:'<user>: <reply_text>'>[Re. <user>]</hover></blue> ",
    var sticker: String = "<blue><hover:show_text:'<sticker_name>'>[sticker]</hover></blue> ",
    var attachment: String = "<blue><hover:show_text:'Open image: <url>'><click:open_url:'<url>'>[attachment]</click></hover></blue> ",
    var link: String = "<blue><hover:show_text:'Open link: <url>'><click:open_url:'<url>'>[link]</click></hover></blue>",
    var customFormats: Map<String, String> = emptyMap(),
) : OkaeriConfig()

