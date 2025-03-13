package top.azimkin.multiMessageBridge.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import top.azimkin.multiMessageBridge.providers.skins.HeadProviderManager

class AsyncHeadImageProviderRegistrationEvent(val manager: HeadProviderManager) : Event(true) {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}