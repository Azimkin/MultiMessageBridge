package top.azimkin.multiMessageBridge.utilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.flattener.ComponentFlattener
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class LegacyTranslator : TranslatorImplementation {
    private val plainSerializer = PlainTextComponentSerializer.builder()
        .flattener(
            ComponentFlattener.textOnly()
                .toBuilder()
                .mapper(TranslatableComponent::class.java) { component ->
                    (translations[component.key()] ?: component.fallback() ?: component.key()).toString()
                        .format(*(component.args().map { translate(it) }).toTypedArray())
                }
                .build()
        )
        .build()

    override var translations: Map<String, Any> = emptyMap()

    override fun translate(component: Component): String {
        return plainSerializer.serialize(component)
    }

    override fun toPlainText(component: Component): String {
        return PlainTextComponentSerializer.plainText().serialize(component)
    }
}
