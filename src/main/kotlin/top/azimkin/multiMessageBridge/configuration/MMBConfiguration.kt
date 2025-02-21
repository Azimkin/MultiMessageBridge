package top.azimkin.multiMessageBridge.configuration

import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.Header

//ez its really cool
@Header("####################################################################")
@Header("#                                                                  #")
@Header("#                                                                  #")
@Header("#             Main configuration of MultiMessageBridge             #")
@Header("# https://github.com/Azimkin/MultiMessageBridge/wiki/Configuration #")
@Header("#                                                                  #")
@Header("#                                                                  #")
@Header("####################################################################")
data class MMBConfiguration(
    @Comment("Should plugin use bStats and publish metrics on https://bstats.org/plugin/bukkit/MultiMessageBridge/24055")
    var metrics: Boolean = true,
    @Comment("This part describes anything about heads?")
    var heads: HeadsConfiguration = HeadsConfiguration(),
    @Comment("Default message receivers that must be enabled")
    var enabledDefaultReceivers: Set<String> = setOf("Minecraft", "Discord", "Telegram"),
    @Comment("Time format used in server info under discord text channel")
    var timeFormat: String = "{d} days {h} hours {m} minutes",
    @Comment("time to update server info in seconds (10min its discord limit)")
    var serverInfoUpdateTime: Int = 60 * 10,
    var defaultServerInfoFormat: String = "Players online: {online} Players total: {total} Uptime: {uptime}",
    @Comment("Should plugin translate death, advancement messages from default locale (English)")
    @Comment("Translations brings from the translations.json file in plugin root folder")
    @Comment("Its just renamed file like https://github.com/InventivetalentDev/minecraft-assets/blob/1.21.4/assets/minecraft/lang/en_us.json")
    var translateMessages: Boolean = false
) : OkaeriConfig()

data class HeadsConfiguration(
    @Comment("Possible replacements depends on your provider")
    @Comment("In default one its: ")
    @Comment("          %nickname% - players nickname")
    @Comment("          %any_placeholder% - any placeholder from placeholderAPI")
    @Comment("More info: https://github.com/Azimkin/MultiMessageBridge/wiki/HeadProviders")
    var url: String = "https://crafthead.net/helm/%nickname%",
    @Comment("Default providers: default")
    var provider: String = "default",
) : OkaeriConfig()

@Header("This section used to configure messages send on your platform")
data class MessageList(
    @Transient private val customMessageType: String = "text",
    @Transient private val customProperties: Map<String, String> = emptyMap(),
    @Comment("Here you can configure how chat message will look on your platform if it sent from every other receiver")
    var messageBase: MessageConfiguration = MessageConfiguration("<platform> <nickname> -> <message>"),
    @Comment("Also here you can configure how chat messages will look from any receiver in that format:")
    @Comment("    customFormats: # just a section name")
    @Comment("      PlatformName: # platform name, for example for discord is Discord")
    @Comment("        format: '<platform> <nickname> -> <message>'")
    @Comment("        type: 'text' # for example for discord it can be embed or image (WIP planed in 0.4)")
    @Comment("        configuration: # type specific configuration can be just {} (empty section)")
    var customFormats: Map<String, MessageConfiguration> = emptyMap(),
    var death: MessageConfiguration = MessageConfiguration("<death_message>", customMessageType, customProperties),
    var join: MessageConfiguration = MessageConfiguration(
        "<nickname> has joined the game!",
        customMessageType,
        customProperties
    ),
    var leave: MessageConfiguration = MessageConfiguration(
        "<nickname> has left the game!",
        customMessageType,
        customProperties
    ),
    var firstJoin: MessageConfiguration = MessageConfiguration(
        "<nickname> has joined the game for first time!",
        customMessageType,
        customProperties
    ),
    var serverEnabled: MessageConfiguration = MessageConfiguration("Server enabled!"),
    var serverDisabled: MessageConfiguration = MessageConfiguration("Server disabled!"),
    @Comment("Possible replacements: ")
    @Comment("  <nickname> - player nickname")
    @Comment("  <advancement> - advancement name")
    @Comment("  <description> - advancement description")
    @Comment("  <rarity> - advancement rarity")
    var advancementGrant: MessageConfiguration = MessageConfiguration("<nickname> has made the advancement <advancement>\n  <description>")
) : OkaeriConfig()

data class MessageConfiguration(
    var format: String = "unknown",
    var type: String = "text",
    var configuration: Map<String, String> = emptyMap(),
) : OkaeriConfig()