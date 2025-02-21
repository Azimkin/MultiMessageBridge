package top.azimkin.multiMessageBridge.platforms

import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.OkaeriConfigInitializer
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer
import top.azimkin.multiMessageBridge.MultiMessageBridge
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

abstract class ConfigurableReceiver<T : OkaeriConfig>(
    name: String,
    val configurationClass: Class<T>,
    var updateFile: Boolean = true,
    customLoadConfig: OkaeriConfigInitializer? = null,
) : BaseReceiver(name) {
    val configFile: File = File(MultiMessageBridge.inst.dataFolder, "$name.yml")
    val config: T = try {
        ConfigManager.create(configurationClass) { customLoadConfig?.apply(it) ?: defaultInitializer(it) }
    } catch (e: Throwable) {
        logger.error("Unable to initialize config for $name", e)
        logger.error("---")
        try {
            Files.copy(
                configFile.toPath(),
                Paths.get(MultiMessageBridge.inst.dataFolder.toPath().toString(), "$name.old.yml"),
                StandardCopyOption.REPLACE_EXISTING
            )
            logger.warn("$name -> Old configuration file was moved into $name.old.yml")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        // clearing an old configuration file to rewrite with new content
        configFile.writeText("")
        ConfigManager.create(configurationClass) { customLoadConfig?.apply(it) ?: defaultInitializer(it) }
    }

    override fun reload() {
        reloadConfig()
    }

    protected fun defaultInitializer(it: OkaeriConfig) {
        it.withConfigurer(YamlBukkitConfigurer())
        it.withBindFile(configFile)
        it.withRemoveOrphans(false)
        it.saveDefaults()
        it.load(updateFile)
    }

    fun reloadConfig() {
        config.load(true)
    }
}