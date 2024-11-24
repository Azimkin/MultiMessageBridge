package top.azimkin.multiMessageBridge.configuration

data class DiscordReceiverConfig(
    val bot: BotConfiguration = BotConfiguration(),
    val messages: MessageList = MessageList(customMessageType = "embed", customProperties = mapOf("color" to "0:0:0")),
    val advanced: AdvancedConfiguration = AdvancedConfiguration()
)

data class BotConfiguration(
    val token: String = "paste token here",
    val guild: Long = -1,
    val channels: Map<String, ChannelConfiguration> = mapOf(
        "messages" to ChannelConfiguration("main_text"),
        "console" to ChannelConfiguration("console"),
    )
)

data class ChannelConfiguration(
    val type: String = "unknown",
    val id: Long = 0,
    val description: String? = null,
)

data class AdvancedConfiguration(
    val jdaProvider: String = "default",
)