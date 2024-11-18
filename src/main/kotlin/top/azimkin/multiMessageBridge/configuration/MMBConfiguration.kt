package top.azimkin.multiMessageBridge.configuration

data class HeadsConfiguration(
    val url: String = "https://crafthead.net/helm/%nickname%",
    val provider: String = "default",
)

data class MMBConfiguration(
    val heads: HeadsConfiguration = HeadsConfiguration(),
)

data class MessageList(
    private val customMessageType: String = "text",
    private val customProperties: Map<String, String> = emptyMap(),
    val messageBase: MessageConfiguration = MessageConfiguration("<platform> <nickname> -> <message>"),
    val customFormats: Map<String, MessageConfiguration> = emptyMap(),
    val death: MessageConfiguration = MessageConfiguration("<death_message>", customMessageType, customProperties),
    val join: MessageConfiguration = MessageConfiguration("<nickname> has joined the game!", customMessageType, customProperties),
    val leave: MessageConfiguration = MessageConfiguration("<nickname> has left the game!", customMessageType, customProperties),
    val firstJoin: MessageConfiguration = MessageConfiguration("<nickname> has joined the game for first time!", customMessageType, customProperties),
    val serverEnabled: MessageConfiguration = MessageConfiguration("Server enabled!"),
    val serverDisabled: MessageConfiguration = MessageConfiguration("Server disabled!"),
)

data class MessageConfiguration(
    val format: String = "unknown",
    val type: String = "text",
    val configuration: Map<String, String> = emptyMap(),
)