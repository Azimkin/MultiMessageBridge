package top.azimkin.multiMessageBridge.skins

import top.azimkin.multiMessageBridge.MultiMessageBridge

class HeadProviderManager {
    val providers: HashMap<String, Class<*>> = HashMap()

    fun add(vararg provider: Pair<String, Class<*>>) {
        providers.putAll(provider)
    }

    fun createByName(name: String, link: String): SkinHeadProvider {
        val linkProvider = LinkHeadProvider(link)
        val clazz = providers[name] ?: return linkProvider

        try {
            if (!SkinHeadProvider::class.java.isAssignableFrom(clazz)) {
                MultiMessageBridge.inst.logger.warning("$name it isn't Skin provider using default!")
                return linkProvider
            }
            return try {
                clazz.getDeclaredConstructor().newInstance()
            } catch (_: Throwable) {
                clazz.getDeclaredConstructor(String::class.java).newInstance(link)
            } as SkinHeadProvider
        } catch (_: Throwable) {
            MultiMessageBridge.inst.logger.warning("Unable to create $name (${clazz.name}) head provider, using default")
            return linkProvider
        }
    }
}