package top.azimkin.multiMessageBridge.platforms

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseReceiver(
    val name: String
) {
    protected val logger: Logger = LoggerFactory.getLogger(name)

    open fun reload() = Unit

    open fun onDisable() {
        logger.info("Receiver $name disabled")
    }
}