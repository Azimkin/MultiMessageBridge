package top.azimkin.multiMessageBridge.api.events

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class AsyncDiscordMessageEvent(val event: MessageReceivedEvent, var mustBeCanceledIfBot: Boolean = true) : Event(true),
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