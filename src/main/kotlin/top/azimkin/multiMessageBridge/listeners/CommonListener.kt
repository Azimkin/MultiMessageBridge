package top.azimkin.multiMessageBridge.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.api.events.ReceiverRegistrationEvent
import top.azimkin.multiMessageBridge.platforms.MinecraftReceiver
import top.azimkin.multiMessageBridge.platforms.TelegramReceiver
import top.azimkin.multiMessageBridge.platforms.discord.DiscordReceiver
import java.util.function.Supplier

object CommonListener : Listener {
    /*
    // Just for test
    @EventHandler
    fun onMessageReceived(event: AsyncChatMessageReceivedEvent) {
        //MultiMessageBridge.inst.logger.info(event.context.toString() + " " + event.dispatcher.toString())
    }

    // Just for test
    @EventHandler
    fun onMessageDispatched(event: AsyncChatMessageDispatchedEvent) {
        //MultiMessageBridge.inst.logger.info(event.context.toString())

    }*/

    @EventHandler
    fun onReceiverRegistration(event: ReceiverRegistrationEvent) {
        event.eventManager.register(
            "Minecraft" to Supplier { MinecraftReceiver(MultiMessageBridge.inst) },
            "Discord" to Supplier { DiscordReceiver(event.eventManager) },
            "Telegram" to Supplier { TelegramReceiver(event.eventManager) },
        )
    }

    @EventHandler
    fun onServerLoaded(event: ServerLoadEvent) {
        MultiMessageBridge.inst.setEnabled()
    }
}