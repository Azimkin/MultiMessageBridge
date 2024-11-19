package top.azimkin.multiMessageBridge.platforms.discord.jdaproviders

class JdaProviderManager {
    val providers: HashMap<String, Class<*>> = HashMap()

    fun add(name: String, clazz: Class<*>) {
        providers[name] = clazz
    }
}