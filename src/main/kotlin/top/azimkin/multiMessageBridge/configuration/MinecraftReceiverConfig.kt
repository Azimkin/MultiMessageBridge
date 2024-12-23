package top.azimkin.multiMessageBridge.configuration

import eu.okaeri.configs.OkaeriConfig

// simplified
data class MinecraftReceiverConfig(
    var messages: MinecraftMessageList = MinecraftMessageList()
) : OkaeriConfig()

// tiny copy of MessageList
data class MinecraftMessageList(
    var messageBase: String = "<platform> <nickname> -> <message>",
    var customFormats: Map<String, String> = emptyMap(),
) : OkaeriConfig()

