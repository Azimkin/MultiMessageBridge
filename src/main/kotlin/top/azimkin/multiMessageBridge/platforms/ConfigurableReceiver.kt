package top.azimkin.multiMessageBridge.platforms

import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.OkaeriConfigInitializer
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer
import top.azimkin.multiMessageBridge.MultiMessageBridge
import java.io.File

open class ConfigurableReceiver<T : OkaeriConfig>(
    name: String,
    val configurationClass: Class<T>,
    var updateFile: Boolean = true,
    customLoadConfig: OkaeriConfigInitializer? = null,
) : BaseReceiver(name) {
    val configFile: File = File(MultiMessageBridge.inst.dataFolder, "$name.yml")
    val config: T = ConfigManager.create(configurationClass) {
        customLoadConfig?.apply(it) ?: defaultInitializer(it)
    }

    override fun reload() {
        reloadConfig()
    }

    protected fun defaultInitializer(it: OkaeriConfig) {
        it.withConfigurer(YamlBukkitConfigurer())
        it.withBindFile(configFile)
        it.withRemoveOrphans(true)
        it.saveDefaults()
        it.load(updateFile)
    }

    fun reloadConfig() {
        config.load(true)
    }
}