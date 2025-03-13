package top.azimkin.multiMessageBridge.providers.skins

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import top.azimkin.multiMessageBridge.MultiMessageBridge

class LinkHeadProvider(val link: String) : SkinHeadProvider {
    val parsePlaceholders = MultiMessageBridge.inst.server.pluginManager.getPlugin("PlaceholderAPI") != null
    val cache = HashMap<String, String>()

    override fun getHeadUrl(player: String): String? {
        var link = try {
            if (parsePlaceholders) PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(player), link) else link
        } catch (e: Exception) {
            MultiMessageBridge.inst.logger.info("Using cached link for $player")
            cache[player]
        }
        link = link?.replace("%nickname%", player)
        link?.let { cache[player] = it }
        return link
    }
}