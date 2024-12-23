package top.azimkin.multiMessageBridge.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment

data class DiscordReceiverConfig(
    var bot: BotConfiguration = BotConfiguration(),
    var messages: MessageList = MessageList(customMessageType = "embed", customProperties = mapOf("color" to "0:0:0")),
    var advanced: AdvancedConfiguration = AdvancedConfiguration()
) : OkaeriConfig()

data class BotConfiguration(
    @Comment("Your discord bot token goes here")
    var token: String = "paste token here",
    @Comment("Guild of your discord server")
    var guild: Long = -1,
    var channels: Map<String, ChannelConfiguration> = mapOf(
        "messages" to ChannelConfiguration("main_text"),
        "console" to ChannelConfiguration("console"),
    )
) : OkaeriConfig()

data class ChannelConfiguration(
    var type: String = "unknown",
    @Comment("Channel id")
    var id: Long = 0,
    var description: String? = null,
) : OkaeriConfig()

data class AdvancedConfiguration(
    @Comment("So you can specify place from where plugin will get JDA")
    @Comment("More info on wiki")
    var jdaProvider: String = "default",
) : OkaeriConfig()