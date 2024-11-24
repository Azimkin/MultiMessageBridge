package top.azimkin.multiMessageBridge.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import top.azimkin.multiMessageBridge.MultiMessageBridge
import java.io.File

class ConfigManager<T>(
    private val configClass: Class<T>,
    private val configFile: File,
    private val objectMapper: ObjectMapper = YAMLMapper(
        YAMLFactory.builder()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .build()
    ).findAndRegisterModules()
) {
    fun loadOrUseDefault(): T {
        return if (configFile.exists()) {
            try {
                val value = objectMapper.readValue(configFile, configClass)
                saveConfig(value)
                value
            } catch (e: Exception) {
                MultiMessageBridge.inst.logger.severe("An error was occurred while trying to read ${configFile.name} configuration: ${e.message}.")
                MultiMessageBridge.inst.logger.info("Using default configuration")
                e.printStackTrace()
                configClass.getDeclaredConstructor().newInstance()
            }
        } else {
            saveDefaultConfig()
        }
    }

    private fun saveDefaultConfig(): T {
        val defaultConfig = configClass.getDeclaredConstructor().newInstance()
        saveConfig(defaultConfig)
        return defaultConfig
    }

    fun saveConfig(config: T) {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config)
    }
}