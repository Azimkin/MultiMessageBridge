package top.azimkin.multiMessageBridge.test

import top.azimkin.multiMessageBridge.ImplementationRegistryImpl
import kotlin.test.Test
import kotlin.test.assertNotNull

class TestImplementationManager {
    @Test
    fun testCasts() {
        val registry = ImplementationRegistryImpl()

        registry.register("basic", TestService::class.java) {TestServiceImpl()}

        val instance = registry.getImplementation(TestService::class.java, "basic")
        assertNotNull(instance)
    }

    interface TestService {
        fun test()
    }

    class TestServiceImpl : TestService {
        override fun test() {
            println("overridden")
        }
    }
}