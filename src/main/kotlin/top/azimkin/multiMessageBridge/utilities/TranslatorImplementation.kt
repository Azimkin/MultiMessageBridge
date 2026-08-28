package top.azimkin.multiMessageBridge.utilities

import net.kyori.adventure.text.Component

interface TranslatorImplementation {
    var translations: Map<String, Any>

    fun translate(component: Component): String

    fun toPlainText(component: Component): String
}
