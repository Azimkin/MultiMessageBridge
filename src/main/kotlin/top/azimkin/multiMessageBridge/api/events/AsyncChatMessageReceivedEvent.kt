package top.azimkin.multiMessageBridge.api.events

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.platforms.BaseReceiver

class AsyncChatMessageReceivedEvent(var context: MessageContext, val dispatcher: BaseReceiver) : Event(true),
    Cancellable {
    private var cancelled: Boolean = false
    override fun getHandlers(): HandlerList = handlerList

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}