package top.azimkin.multiMessageBridge.platforms

import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.configuration.ConfigManager
import java.io.File

open class ConfigurableReceiver<T>(name: String, val configuration: Class<T>) : BaseReceiver(name) {
    protected val configManager: ConfigManager<T> =
        ConfigManager(configuration, File(MultiMessageBridge.inst.dataFolder, "$name.yml"))
    var config: T = configManager.loadOrUseDefault()
        protected set

    override fun reload() {
        reloadConfig()
    }

    fun reloadConfig() {
        config = configManager.loadOrUseDefault()
    }
}