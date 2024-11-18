package top.azimkin.multiMessageBridge.platforms

import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.configuration.ConfigManager
import java.io.File

abstract class BaseReceiver(
    val name: String
) {
    open fun reload() = Unit

    open fun onDisable() = Unit
}