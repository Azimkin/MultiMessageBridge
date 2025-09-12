package top.azimkin.multiMessageBridge.api.events

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.platforms.MinecraftReceiver

class MinecraftChatMessageReceivedEvent(val ctx: MessageContext, val receiver: MinecraftReceiver) : Event(),
    Cancellable {
    private var cancelled = false
    override fun getHandlers(): HandlerList = handlerList

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(state: Boolean) {
        cancelled = state
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}