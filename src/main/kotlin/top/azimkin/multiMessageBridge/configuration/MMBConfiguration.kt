package top.azimkin.multiMessageBridge.configuration

import eu.okaeri.configs.OkaeriConfig

data class MMBConfiguration(
    var metrics: Boolean = true,
    var heads: HeadsConfiguration = HeadsConfiguration(),
    var enabledDefaultReceivers: Set<String> = setOf("Minecraft", "Discord", "Telegram"),
    var timeFormat: String = "{d} days {h} hours {m} minutes",
    var serverInfoUpdateTime: Int = 60,
    var defaultServerInfoFormat: String = "Players online: {online} Players total: {total} Uptime: {uptime}",
) : OkaeriConfig()

data class HeadsConfiguration(
    var url: String = "https://crafthead.net/helm/%nickname%",
    var provider: String = "default",
) : OkaeriConfig()

data class MessageList(
    @Transient private val customMessageType: String = "text",
    @Transient private val customProperties: Map<String, String> = emptyMap(),
    var messageBase: MessageConfiguration = MessageConfiguration("<platform> <nickname> -> <message>"),
    var customFormats: Map<String, MessageConfiguration> = emptyMap(),
    var death: MessageConfiguration = MessageConfiguration("<death_message>", customMessageType, customProperties),
    var join: MessageConfiguration = MessageConfiguration("<nickname> has joined the game!", customMessageType, customProperties),
    var leave: MessageConfiguration = MessageConfiguration("<nickname> has left the game!", customMessageType, customProperties),
    var firstJoin: MessageConfiguration = MessageConfiguration("<nickname> has joined the game for first time!", customMessageType, customProperties),
    var serverEnabled: MessageConfiguration = MessageConfiguration("Server enabled!"),
    var serverDisabled: MessageConfiguration = MessageConfiguration("Server disabled!"),
) : OkaeriConfig()

data class MessageConfiguration(
    var format: String = "unknown",
    var type: String = "text",
    var configuration: Map<String, String> = emptyMap(),
) : OkaeriConfig()