package top.azimkin.multiMessageBridge.configuration

import eu.okaeri.configs.OkaeriConfig

data class DiscordReceiverConfig(
    var bot: BotConfiguration = BotConfiguration(),
    var messages: MessageList = MessageList(customMessageType = "embed", customProperties = mapOf("color" to "0:0:0")),
    var advanced: AdvancedConfiguration = AdvancedConfiguration()
) : OkaeriConfig()

data class BotConfiguration(
    var token: String = "paste token here",
    var guild: Long = -1,
    var channels: Map<String, ChannelConfiguration> = mapOf(
        "messages" to ChannelConfiguration("main_text"),
        "console" to ChannelConfiguration("console"),
    )
) : OkaeriConfig()

data class ChannelConfiguration(
    var type: String = "unknown",
    var id: Long = 0,
    var description: String? = null,
) : OkaeriConfig()

data class AdvancedConfiguration(
    var jdaProvider: String = "default",
) : OkaeriConfig()