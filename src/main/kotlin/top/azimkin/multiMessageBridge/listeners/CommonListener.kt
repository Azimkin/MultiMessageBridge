package top.azimkin.multiMessageBridge.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.api.events.AsyncChatMessageDispatchedEvent
import top.azimkin.multiMessageBridge.api.events.AsyncChatMessageReceivedEvent
import top.azimkin.multiMessageBridge.api.events.ReceiverRegistrationEvent
import top.azimkin.multiMessageBridge.platforms.MinecraftReceiver
import top.azimkin.multiMessageBridge.platforms.TelegramReceiver
import top.azimkin.multiMessageBridge.platforms.discord.DiscordReceiver

object CommonListener : Listener {
    // Just for test
    @EventHandler
    fun onMessageReceived(event: AsyncChatMessageReceivedEvent) {
        //MultiMessageBridge.inst.logger.info(event.context.toString() + " " + event.dispatcher.toString())
    }

    // Just for test
    @EventHandler
    fun onMessageDispatched(event: AsyncChatMessageDispatchedEvent) {
        //MultiMessageBridge.inst.logger.info(event.context.toString())

    }

    @EventHandler
    fun onReceiverRegistration(event: ReceiverRegistrationEvent) {
        event.eventManager.register(
            MinecraftReceiver(MultiMessageBridge.inst),
            TelegramReceiver,
            DiscordReceiver(),
        )
    }
}