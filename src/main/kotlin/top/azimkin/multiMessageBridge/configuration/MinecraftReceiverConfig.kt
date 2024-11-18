package top.azimkin.multiMessageBridge.configuration

data class MinecraftReceiverConfig(
    val messages: MessageList = MessageList(customFormats = mapOf("Telegram" to MessageConfiguration("[TG] <nickname> -> <message>"))),
)
