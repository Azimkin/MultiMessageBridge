package top.azimkin.multiMessageBridge.utilities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import top.azimkin.multiMessageBridge.MultiMessageBridge
import java.io.File

/**
 * Translates your component using translations file (like default minecraft translations file)
 *
 * So if you know better way to do it - do it :D
 */
object Translator {
    private const val POST_26_IMPLEMENTATION =
        "top.azimkin.multiMessageBridge.versions.post26.Post26Translator"

    private val implementation: TranslatorImplementation = createImplementation()

    var translations: Map<String, Any>
        get() = implementation.translations
        set(value) {
            implementation.translations = value
        }

    fun translate(component: Component): String {
        return implementation.translate(component)
    }

    fun optional(component: Component): String {
        return if (MultiMessageBridge.inst.pluginConfig.translateMessages) {
            implementation.translate(component)
        } else {
            implementation.toPlainText(component)
        }
    }

    fun reload() {
        //just an empty map
        translations = mapOf()
        val file = File(MultiMessageBridge.inst.dataFolder, "translations.json")
        if (file.exists() && MultiMessageBridge.inst.pluginConfig.translateMessages) {
            try {
                val type = object : TypeToken<Map<String, Any>>() {}.type
                translations = Gson().fromJson(file.readText(), type)
                MultiMessageBridge.inst.logger.info("${translations.size} translations loaded!")
            } catch (e: Throwable) {
                MultiMessageBridge.inst.logger.severe("Unable to load translations!")
                e.printStackTrace()
            }

        }
    }

    private fun createImplementation(): TranslatorImplementation {
        val minecraftVersion = runCatching { Bukkit.getMinecraftVersion() }.getOrNull()
        if (minecraftVersion != null && usesPost26Translator(minecraftVersion)) {
            return try {
                Class.forName(POST_26_IMPLEMENTATION)
                    .getDeclaredConstructor()
                    .newInstance() as TranslatorImplementation
            } catch (error: ReflectiveOperationException) {
                throw IllegalStateException("Unable to load the post-1.21.11 translator implementation", error)
            }
        }
        return LegacyTranslator()
    }
}

private val LEGACY_MAX_VERSION = listOf(1, 21, 11)
private val MINECRAFT_VERSION_PATTERN = Regex("""^(\d+)(?:\.(\d+))?(?:\.(\d+))?""")

internal fun usesPost26Translator(version: String): Boolean {
    val match = MINECRAFT_VERSION_PATTERN.find(version) ?: return false
    val parsed = (1..3).map { index -> match.groupValues[index].toIntOrNull() ?: 0 }

    for (index in LEGACY_MAX_VERSION.indices) {
        if (parsed[index] != LEGACY_MAX_VERSION[index]) {
            return parsed[index] > LEGACY_MAX_VERSION[index]
        }
    }
    return false
}
