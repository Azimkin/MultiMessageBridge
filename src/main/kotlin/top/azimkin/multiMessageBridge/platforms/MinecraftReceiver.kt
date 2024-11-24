package top.azimkin.multiMessageBridge.platforms

import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.plugin.java.JavaPlugin
import org.json.JSONObject
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.configuration.MinecraftReceiverConfig
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.data.PlayerLifeContext
import top.azimkin.multiMessageBridge.data.ServerSessionContext
import top.azimkin.multiMessageBridge.data.SessionContext
import top.azimkin.multiMessageBridge.platforms.dispatchers.MessageDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.PlayerLifeDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.ServerSessionDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.SessionDispatcher
import top.azimkin.multiMessageBridge.platforms.handlers.MessageHandler
import top.azimkin.multiMessageBridge.utilities.deserialize
import top.azimkin.multiMessageBridge.utilities.runSync
import top.azimkin.multiMessageBridge.utilities.toHex
import top.azimkin.multiMessageBridge.utilities.toPlainText
import java.awt.Color
import java.io.File

class MinecraftReceiver(val plugin: JavaPlugin) :
    ConfigurableReceiver<MinecraftReceiverConfig>("Minecraft", MinecraftReceiverConfig::class.java), Listener,
    MessageHandler, MessageDispatcher, PlayerLifeDispatcher, ServerSessionDispatcher, SessionDispatcher {

    var deathMessages = JSONObject()

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        updateDeathMessages()
    }


    override fun handle(context: MessageContext) {
        val baseMessage = getMessageOrBase(context.platform)
        if (context.message.startsWith("https://") || context.message.startsWith("http://")) return
        val formattedMessage = deserialize(
            baseMessage,
            mapOf(
                "platform" to context.platform,
                "role" to coloredRole(context.role, context.roleColor),
                "nickname" to context.senderName,
                "message" to context.message
            )
        )
        runSync {
            for (player in Bukkit.getOnlinePlayers()) {
                player.sendMessage(formattedMessage)
            }
        }
    }

    private fun coloredRole(role: String?, color: Color?): String {
        return (if (color != null) "<color:${color.toHex()}>" else "") + (role ?: "")
    }

    private fun getMessageOrBase(platform: String): String =
        config.messages.customFormats[platform] ?: config.messages.messageBase

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onMessage(event: AsyncChatEvent) {
        if (event.isCancelled) return
        dispatch(
            MessageContext(
                event.player.name,
                event.message().toPlainText(),
                false,
                name,
                null,
                null,
                MultiMessageBridge.inst.metadataProvider.getPrefix(event.player)
            )
        )
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val plain = event.deathMessage()?.toPlainText() ?: ""
        val messageToSend = if (config.translateDeathMessages) {
            var key = plain.replace(event.player.displayName().toPlainText(), "%1\$s")
            val cause = event.player.lastDamageCause
            var damager = ""
            if (cause is EntityDamageByEntityEvent) {
                damager = cause.damager.customName()?.toPlainText() ?: cause.damager.name
                key = key.replace(damager, "%2\$s")
            } else if (cause is EntityDamageByBlockEvent) {
                damager = cause.damager?.type?.name?.lowercase() ?: "block"
                key = key.replace(damager, "%2\$s")
            } else {
                println("Unknown cause: $cause")
            }
            try {
                deathMessages.getString(key)
            } catch (_: Exception) {
                plain
            }.replace("%1\$s", event.player.displayName().toPlainText()).replace("%2\$s", damager)
        } else plain

        dispatch(PlayerLifeContext(event.player.name, messageToSend))
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        dispatch(SessionContext(event.player.name, true, !event.player.hasPlayedBefore()))
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        dispatch(SessionContext(event.player.name, false))
    }

    @EventHandler
    fun onServerEnable(event: ServerLoadEvent) {
        dispatch(ServerSessionContext(true, event.type == ServerLoadEvent.LoadType.RELOAD))
    }

    override fun onDisable() {
        dispatch(ServerSessionContext(false))
    }

    override fun reload() {
        super.reload()
        updateDeathMessages()
    }

    private fun updateDeathMessages() {
        if (config.translateDeathMessages) {
            plugin.saveResource("death_messages.json", false)
            deathMessages = JSONObject(File(plugin.dataFolder, "death_messages.json").readText())
        }
    }
}