package top.azimkin.multiMessageBridge.api.events

import com.pengrad.telegrambot.model.Update
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class AsyncTelegramOnUpdateEvent(val update: Update) : Event(true), Cancellable {
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