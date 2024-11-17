package top.azimkin.multiMessageBridge.platforms.discord.jdaproviders

class JdaProviderManager {
    val providers: HashMap<String, Class<*>> = HashMap()

    fun add(vararg providers: Pair<String, Class<*>>) {
        providers.forEach { this.providers[it.first] = it.second }
    }
}