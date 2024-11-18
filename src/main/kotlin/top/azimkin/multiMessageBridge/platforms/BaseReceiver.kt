package top.azimkin.multiMessageBridge.platforms

import top.azimkin.multiMessageBridge.configuration.ReceiverConfig
import top.azimkin.multiMessageBridge.configuration.YamlReceiverConfiguration

abstract class BaseReceiver(
    val name: String,
    val defaultConfiguration: Map<String, Any> = emptyMap()
) {
    val config: ReceiverConfig = YamlReceiverConfiguration(name, defaultConfiguration)

    open fun reload() {
        config.reload()
    }

    open fun onDisable() {

    }
}