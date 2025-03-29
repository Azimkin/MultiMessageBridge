package top.azimkin.multiMessageBridge.utilities

import net.kyori.adventure.text.Component
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

fun Component.toMiniMessage(): String {
    return MiniMessage.miniMessage().serialize(this)
}

fun String.fbm(replacements: Map<String, String>) = formatByMap(replacements)

private val angleBracketsRegex = Regex("<(\\w+)>")
fun String.formatByMap(replacements: Map<String, String>): String {
    return angleBracketsRegex.replace(this) { matchResult ->
        val key = matchResult.groupValues[1]
        angleBracketsRegex.replace(replacements[key] ?: matchResult.value) { mr -> replacements[key] ?: mr.value }
    }
}