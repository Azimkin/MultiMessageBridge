package top.azimkin.multiMessageBridge

import java.util.function.Supplier


interface ImplementationRegistry {
    fun <T, I : T> register(name: String, baseInterface: Class<T>, implementation: Supplier<I>);

    fun <T> getImplementation(baseInterface: Class<T>, name: String): T?

    fun <T> getImplementationCreator(baseInterface: Class<T>, name: String): Supplier<T>?

    fun <T> getImplementations(baseInterface: Class<T>): Map<String, Supplier<T>>
}