package top.azimkin.multiMessageBridge.platforms.discord.jdaproviders

import top.azimkin.multiMessageBridge.platforms.discord.DiscordReceiver
import java.util.function.BiFunction

class Discord4jProviderManager {
    val providers: HashMap<String, BiFunction<String, DiscordReceiver, DiscordProvider>> = HashMap()

    fun add(name: String, initFunction: BiFunction<String, DiscordReceiver, DiscordProvider>) {
        providers[name] = initFunction
    }
}