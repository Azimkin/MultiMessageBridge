package top.azimkin.multiMessageBridge.utilities.configuration

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import top.azimkin.multiMessageBridge.MultiMessageBridge
import java.io.File

class YamlReceiverConfiguration(name: String, defaults: Map<String, Any>) : ReceiverConfig(name, defaults) {
    override val extension: String = "yml"
    private val file: File = File(MultiMessageBridge.inst.dataFolder, "$name.$extension").also {
        if (!it.exists()) it.createNewFile()
    }
    private var fileConfiguration: FileConfiguration = YamlConfiguration.loadConfiguration(file)

    init {
        defaults.forEach { (key, value) ->
            if (!isSet(key)) set(key, value)
        }
        save()
    }

    override fun set(key: String, value: Any) = fileConfiguration.set(key, value)

    override fun isSet(key: String): Boolean = fileConfiguration.isSet(key)

    override fun getInt(key: String) = fileConfiguration.getInt(key)

    override fun getLong(key: String): Long = fileConfiguration.getLong(key)

    override fun getStringList(key: String): List<String> = fileConfiguration.getStringList(key)

    override fun getString(key: String): String? = fileConfiguration.getString(key)

    override fun getTranslation(key: String): String = fileConfiguration.getString(key, null) ?: key

    override fun getFile(): File = file

    override fun getBoolean(key: String): Boolean = fileConfiguration.getBoolean(key)

    override fun save() = fileConfiguration.save(file)

    override fun reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file)
    }
}