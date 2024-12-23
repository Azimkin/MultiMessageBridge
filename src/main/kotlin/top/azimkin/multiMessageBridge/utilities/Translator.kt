package top.azimkin.multiMessageBridge.utilities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.flattener.ComponentFlattener
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import top.azimkin.multiMessageBridge.MultiMessageBridge
import java.io.File

/**
 * Translates your component using translations file (like default minecraft translations file)
 *
 * So if you know better way to do it - do it :D
 */
object Translator {
    val plainSerializer = PlainTextComponentSerializer.builder()
        .flattener(
            ComponentFlattener.textOnly()
                .toBuilder()
                .mapper<TranslatableComponent>(TranslatableComponent::class.java) { component ->
                    return@mapper (translations[component.key()] ?: component.fallback() ?: component.key()).toString()
                        .format(*(component.args().map { translate(it) }).toTypedArray())
                }.build()
        ).build()

    var translations: Map<String, Any> = mapOf()

    fun translate(component: Component): String {
        val rawString = plainSerializer.serialize(component)
        return rawString
    }

    fun optional(component: Component): String {
        return if (MultiMessageBridge.inst.pluginConfig.translateMessages) translate(component) else component.toPlainText()
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
}