package top.azimkin.multiMessageBridge.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment

data class DiscordReceiverConfig(
    var bot: BotConfiguration = BotConfiguration(),
    var messages: MessageList = MessageList(customMessageType = "embed", customProperties = mapOf("color" to "0:0:0")),
    var advanced: AdvancedConfiguration = AdvancedConfiguration(),
    var phraseFilter: PhraseFilter = PhraseFilter(),
) : OkaeriConfig()

data class BotConfiguration(
    @Comment("Your discord bot token goes here")
    var token: String = "paste token here",
    @Comment("Guild of your discord server")
    var guild: Long = -1,
    var channels: Map<String, ChannelConfiguration> = mapOf(
        "messages" to ChannelConfiguration("main_text"),
        "console" to ChannelConfiguration("console"),
    ),
    @Comment("Should plugin ignore messages in console without prefix in start")
    var commandsShouldStartsWithPrefix: Boolean = true,
    var commandPrefix: String = "!/"
) : OkaeriConfig()

data class ChannelConfiguration(
    var type: String = "unknown",
    @Comment("Channel id")
    var id: Long = 0,
    var description: String? = null,
) : OkaeriConfig()

data class AdvancedConfiguration(
    @Comment("So you can specify place from where plugin will get JDA")
    @Comment("More info on wiki")
    var jdaProvider: String = "default",
) : OkaeriConfig()

data class PhraseFilter(
    @Comment("This part allows you to configure replacements for phrases like @everyone, @here")
    var filterMessages: Boolean = true,
    var filters: Map<String, String> = mapOf(
        "@everyone" to "[everyone]",
        "@here" to "[here]",
    )
) : OkaeriConfig()