package top.azimkin.multiMessageBridge.skins

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import top.azimkin.multiMessageBridge.MultiMessageBridge

class LinkHeadProvider(val link: String) : SkinHeadProvider {
    val parsePlaceholders = MultiMessageBridge.inst.server.pluginManager.getPlugin("PlaceholderAPI") != null
    override fun getHeadUrl(player: String): String? {
        var link = if (parsePlaceholders) PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(player), link) else link
        return link.replace("%nickname%", player)
    }
}