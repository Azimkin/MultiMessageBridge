package top.azimkin.multiMessageBridge

interface ImplementationRegistry {
    fun <T> register(name: String, baseInterface: Class<T>, implementation: () -> Any);

    fun <T> getImplementation(baseInterface: Class<T>, name: String): T?

    fun <T> getImplementationCreator(baseInterface: Class<T>, name: String): (() -> T)?

    fun <T> getImplementations(baseInterface: Class<T>): Map<String, () -> T>
}