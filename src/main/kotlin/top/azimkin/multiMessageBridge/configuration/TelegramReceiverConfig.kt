package top.azimkin.multiMessageBridge.configuration

import com.fasterxml.jackson.annotation.JsonPropertyDescription

data class TelegramReceiverConfig(
    val bot: TelegramBotConfig = TelegramBotConfig(),
    val messages: MessageList = MessageList(),
    val debug: DebugTelegramBotConfig = DebugTelegramBotConfig()
)

data class TelegramBotConfig(
    val token: String = "paste token here",
    val mainChat: Long = -1,
    val mainThread: Int = -1
)

data class DebugTelegramBotConfig(
    val preConfiguredDebug: Boolean = true,
    val logPackets: Boolean = false,
)