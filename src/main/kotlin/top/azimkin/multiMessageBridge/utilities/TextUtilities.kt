package top.azimkin.multiMessageBridge.utilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.flattener.ComponentFlattener
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun deserialize(text: String, replacements: Map<String, String> = emptyMap(), player: Player? = null): Component {
    val mappedReplacements = replacements.map { (key, value) -> Placeholder.parsed(key, value) }.toTypedArray()
    return MiniMessage.miniMessage().deserialize(text, *mappedReplacements)
}

fun CommandSender.send(message: String, replacements: Map<String, String> = emptyMap()) {
    this.sendMessage(deserialize(message, replacements))
}

fun Component.toPlainText(): String {
    return PlainTextComponentSerializer.plainText().serialize(this)
}

val translatablePlainSerializer = PlainTextComponentSerializer
    .builder()
    .flattener(ComponentFlattener
        .basic()
        .toBuilder()
        .mapper(TranslatableComponent::class.java) { component -> component.key() }
        .build()
    ).build()
val advancedTranslatablePlainSerializer = PlainTextComponentSerializer
    .builder()
    .flattener(ComponentFlattener
        .basic()
        .toBuilder()
        .mapper(TranslatableComponent::class.java) { component ->
            component.key() + "*|*" + component.args().map { translatablePlainSerializer.serialize(it) }.joinToString("*|*")
        }
        .build()
    ).build()

fun Component.toPlainTextWithTranslatableAndArgs(): String {
    val split = advancedTranslatablePlainSerializer.serialize(this).split("*|*")
    var text = split[0]
    val args = split.subList(1, split.size)
    for ((i, j) in args.withIndex()) {
        if (i != 0) {
            text = text.replace("%${i+1}\$s", j)
        } else {
            text = text.replace("%s", j).replace("%1\$s", j)
        }
    }
    return text
}

fun Component.toMiniMessage(): String {
    return MiniMessage.miniMessage().serialize(this)
}

fun String.format(replacements: Map<String, String>): String {
    return Regex("<(\\w+)>").replace(this) { matchResult ->
        val key = matchResult.groupValues[1]
        replacements[key] ?: matchResult.value // Если ключа нет в карте, оставляем исходный тег
    }
}