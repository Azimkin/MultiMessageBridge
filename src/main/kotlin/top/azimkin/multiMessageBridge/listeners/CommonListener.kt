package top.azimkin.multiMessageBridge.listeners

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import org.slf4j.LoggerFactory
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.api.events.AsyncChatMessageDispatchedEvent
import top.azimkin.multiMessageBridge.api.events.ChatHandlerRegistrationEvent
import top.azimkin.multiMessageBridge.api.events.ImplementationsRegistrationEvent
import top.azimkin.multiMessageBridge.handlers.chat.ChatExChatHandler
import top.azimkin.multiMessageBridge.handlers.chat.EssentialsChatHandler
import top.azimkin.multiMessageBridge.handlers.chat.NoPluginChatHandler
import top.azimkin.multiMessageBridge.platforms.BaseReceiver
import top.azimkin.multiMessageBridge.platforms.MinecraftReceiver
import top.azimkin.multiMessageBridge.platforms.TelegramReceiver
import top.azimkin.multiMessageBridge.platforms.discord.DiscordReceiver
import java.util.function.Supplier

object CommonListener : Listener {
    private val logger = LoggerFactory.getLogger("MMB")

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChatHandlerRegistration(event: ChatHandlerRegistrationEvent) {
        event.chatHandler = if (Bukkit.getPluginManager().getPlugin("ChatEx") != null) {
            ChatExChatHandler(event.minecraftReceiver, event.configuration)
        } else if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            EssentialsChatHandler(event.minecraftReceiver)
        } else {
            NoPluginChatHandler(event.minecraftReceiver)
        }
    }

    @EventHandler
    fun onMessageReceived(event: AsyncChatMessageDispatchedEvent) {
        if (MultiMessageBridge.inst.pluginConfig.sendMessagesToConsole && event.context.platform != "Minecraft")
            logger.info("[${event.context.platform}] ${event.context.senderName} -> ${event.context.message}")
    }

    @EventHandler
    fun onServerLoaded(event: ServerLoadEvent) {
        MultiMessageBridge.inst.setEnabled()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onImplementationRegistration(event: ImplementationsRegistrationEvent) {
        val implementationRegistry = event.registry
        val pl = MultiMessageBridge.inst
        implementationRegistry.register("Minecraft", BaseReceiver::class.java) { MinecraftReceiver(pl) }
        implementationRegistry.register(
            "Telegram",
            BaseReceiver::class.java
        ) { TelegramReceiver(pl.messagingEventManager) }
        implementationRegistry.register(
            "Discord",
            BaseReceiver::class.java
        ) { DiscordReceiver(pl.messagingEventManager) }
    }
}