package top.azimkin.multiMessageBridge.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import top.azimkin.multiMessageBridge.platforms.BaseReceiver

class ReceiverEnabledEvent(var receiver: BaseReceiver) : Event() {
    override fun getHandlers(): HandlerList  = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}