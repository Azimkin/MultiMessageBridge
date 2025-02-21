package top.azimkin.multiMessageBridge.platforms.discord.jdaproviders

import top.azimkin.multiMessageBridge.platforms.discord.DiscordReceiver

class JdaProviderManager {
    val providers: HashMap<String, (String, DiscordReceiver) -> JdaProvider> = HashMap()

    fun add(name: String, initFunction: (String, DiscordReceiver) -> JdaProvider) {
        providers[name] = initFunction
    }
}