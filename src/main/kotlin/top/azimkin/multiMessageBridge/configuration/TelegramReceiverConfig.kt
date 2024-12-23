package top.azimkin.multiMessageBridge.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment

data class TelegramReceiverConfig(
    var bot: TelegramBotConfig = TelegramBotConfig(),
    var messages: MessageList = MessageList(),
    var debug: DebugTelegramBotConfig = DebugTelegramBotConfig()
) : OkaeriConfig()

data class TelegramBotConfig(
    var token: String = "paste token here",
    @Comment("Id of your channel. Also it can be just a PM with bot")
    var mainChat: Long = -1,
    @Comment("If you using threads system it must be greater than -1")
    var mainThread: Int = -1
) : OkaeriConfig()

data class DebugTelegramBotConfig(
    @Comment("Must bot send debug info like chat id, thread id, message")
    var preConfiguredDebug: Boolean = true,
    var logPackets: Boolean = false,
) : OkaeriConfig()