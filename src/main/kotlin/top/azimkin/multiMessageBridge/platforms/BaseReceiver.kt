package top.azimkin.multiMessageBridge.platforms

abstract class BaseReceiver(
    val name: String
) {
    open fun reload() = Unit

    open fun onDisable() = Unit
}