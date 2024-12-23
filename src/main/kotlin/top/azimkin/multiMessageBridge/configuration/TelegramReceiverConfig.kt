package top.azimkin.multiMessageBridge.configuration

import eu.okaeri.configs.OkaeriConfig

data class TelegramReceiverConfig(
    var bot: TelegramBotConfig = TelegramBotConfig(),
    var messages: MessageList = MessageList(),
    var debug: DebugTelegramBotConfig = DebugTelegramBotConfig()
) : OkaeriConfig()

data class TelegramBotConfig(
    var token: String = "paste token here",
    var mainChat: Long = -1,
    var mainThread: Int = -1
) : OkaeriConfig()

data class DebugTelegramBotConfig(
    var preConfiguredDebug: Boolean = true,
    var logPackets: Boolean = false,
) : OkaeriConfig()