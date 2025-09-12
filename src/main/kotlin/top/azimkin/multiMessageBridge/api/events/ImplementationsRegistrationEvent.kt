package top.azimkin.multiMessageBridge.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import top.azimkin.multiMessageBridge.ImplementationRegistry
import top.azimkin.multiMessageBridge.MessagingEventManager

class ImplementationsRegistrationEvent(val registry: ImplementationRegistry) : Event() {

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }

    override fun getHandlers(): HandlerList = HANDLERS
}