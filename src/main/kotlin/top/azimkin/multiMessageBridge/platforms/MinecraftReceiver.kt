package top.azimkin.multiMessageBridge.platforms

import io.papermc.paper.advancement.AdvancementDisplay
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.plugin.java.JavaPlugin
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.api.events.ChatHandlerRegistrationEvent
import top.azimkin.multiMessageBridge.api.events.MinecraftChatMessageReceivedEvent
import top.azimkin.multiMessageBridge.configuration.MinecraftReceiverConfig
import top.azimkin.multiMessageBridge.data.*
import top.azimkin.multiMessageBridge.handlers.chat.ChatHandler
import top.azimkin.multiMessageBridge.handlers.chat.NoPluginChatHandler
import top.azimkin.multiMessageBridge.platforms.dispatchers.*
import top.azimkin.multiMessageBridge.platforms.handlers.MessageHandler
import top.azimkin.multiMessageBridge.utilities.*
import java.awt.Color

class MinecraftReceiver(val plugin: JavaPlugin) :
    ConfigurableReceiver<MinecraftReceiverConfig>("Minecraft", MinecraftReceiverConfig::class.java), Listener,
    MessageHandler, MessageDispatcher, PlayerLifeDispatcher, ServerSessionDispatcher, SessionDispatcher,
    AdvancementDispatcher {
    var chatHandler: ChatHandler; private set

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        val event = ChatHandlerRegistrationEvent(config.chatHandlerConfiguration, this)
        event.callEvent()
        chatHandler = (event.chatHandler ?: run {
            logger.warn("No chat handler was specified in event, using NoPluginChatHandler!")
            NoPluginChatHandler(this)
        }).apply {
            addListener(this@MinecraftReceiver::dispatch)
            addListener { ctx -> runSync { MinecraftChatMessageReceivedEvent(ctx, this@MinecraftReceiver) } }
        }
        logger.info("Using ${chatHandler.javaClass.simpleName} as chat handler!")
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

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val plain = event.deathMessage()?.toPlainText() ?: ""
        val messageToSend = if (MultiMessageBridge.inst.pluginConfig.translateMessages) {
            Translator.translate(event.deathMessage() ?: Component.empty())
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

    @EventHandler
    fun onAdvancement(event: PlayerAdvancementDoneEvent) {
        if (!config.dispatchAdvancements) return
        val rarity = event.advancement.display?.frame() ?: AdvancementDisplay.Frame.TASK
        if (config.filterAdvancements && rarity !in config.enabledAdvancementRarity()) return
        dispatch(
            AdvancementContext(
                event.player.name,
                Translator.optional(event.advancement.displayName()),
                Translator.optional(event.advancement.display?.description() ?: Component.empty()),
                rarity
            )
        )
    }

    override fun onDisable() {
        dispatch(ServerSessionContext(false))
    }
}