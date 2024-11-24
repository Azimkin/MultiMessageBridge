package top.azimkin.multiMessageBridge.server

import org.bukkit.Bukkit
import top.azimkin.multiMessageBridge.MultiMessageBridge
import java.util.regex.Matcher
import java.util.regex.Pattern

object ServerInfoProvider {
    val replacements = hashMapOf<String, () -> String>(
        "online" to { Bukkit.getOnlinePlayers().size.toString() },
        "uptime" to { MultiMessageBridge.inst.dateFormatter.format(MultiMessageBridge.inst.uptime) },
        "total" to { Bukkit.getOfflinePlayers().size.toString() },
    )

    fun parse(text: String?): String? {
        if (text == null) return null
        val pattern = Pattern.compile("\\{(\\S+?)}")
        val matcher = pattern.matcher(text)

        val result = StringBuilder()

        while (matcher.find()) {
            val key = matcher.group(1)

            val replacement = replacements.getOrDefault(key) { matcher.group(0) }()

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement))
        }

        matcher.appendTail(result)

        return result.toString()
    }
}