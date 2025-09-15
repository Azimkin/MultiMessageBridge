package top.azimkin.multiMessageBridge

import java.util.function.Supplier

class ImplementationRegistryImpl : ImplementationRegistry {
    private val registry = hashMapOf<Class<*>, HashMap<String, Supplier<*>>>()

    override fun <T, I : T> register(
        name: String,
        baseInterface: Class<T>,
        implementation: Supplier<I>
    ) {
        registry.putIfAbsent(baseInterface, hashMapOf())
        registry[baseInterface]!![name] = implementation
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getImplementation(baseInterface: Class<T>, name: String): T? {
        return registry[baseInterface]?.get(name)?.get() as? T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getImplementationCreator(baseInterface: Class<T>, name: String): Supplier<T>? {
        return registry[baseInterface]?.get(name) as? Supplier<T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getImplementations(baseInterface: Class<T>): Map<String, Supplier<T>> {
        return registry[baseInterface] as? Map<String, Supplier<T>> ?: hashMapOf()
    }
}