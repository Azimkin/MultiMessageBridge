package top.azimkin.multiMessageBridge.versions.post26

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.flattener.ComponentFlattener
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import top.azimkin.multiMessageBridge.utilities.TranslatorImplementation

class Post26Translator : TranslatorImplementation {
    private val plainSerializer = PlainTextComponentSerializer.builder()
        .flattener(
            ComponentFlattener.builder()
                .mapper(TextComponent::class.java) { component -> component.content() }
                .mapper(TranslatableComponent::class.java) { component ->
                    (translations[component.key()] ?: component.fallback() ?: component.key()).toString()
                        .format(
                            *(component.arguments().map { argument ->
                                val value = argument.value()
                                if (value is Component) translate(value) else value
                            }).toTypedArray()
                        )
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
