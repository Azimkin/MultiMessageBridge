package top.azimkin.multiMessageBridge.configuration

// simplified
data class MinecraftReceiverConfig(
    val messages: MinecraftMessageList = MinecraftMessageList(),
    val translateDeathMessages: Boolean = false
)

// tiny copy of MessageList
data class MinecraftMessageList(
    val messageBase: String = "<platform> <nickname> -> <message>",
    val customFormats: Map<String, String> = emptyMap(),
)

