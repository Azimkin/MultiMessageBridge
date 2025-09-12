package top.azimkin.multiMessageBridge

class ImplementationRegistryImpl : ImplementationRegistry {
    private val registry = hashMapOf<Class<*>, HashMap<String, () -> Any>>()

    override fun <T> register(name: String, baseInterface: Class<T>, implementation: () -> Any) {
        registry.putIfAbsent(baseInterface, hashMapOf())
        registry[baseInterface]!![name] = implementation
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getImplementation(baseInterface: Class<T>, name: String): T? {
        return registry[baseInterface]?.get(name)?.invoke() as? T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getImplementationCreator(baseInterface: Class<T>, name: String): (() -> T)? {
        return registry[baseInterface]?.get(name) as? () -> T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getImplementations(baseInterface: Class<T>): Map<String, () -> T> {
        return registry[baseInterface] as? Map<String, () -> T> ?: hashMapOf()
    }
}