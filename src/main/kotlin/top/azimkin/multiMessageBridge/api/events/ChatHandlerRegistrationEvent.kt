package top.azimkin.multiMessageBridge.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import top.azimkin.multiMessageBridge.handlers.chat.ChatHandler
import top.azimkin.multiMessageBridge.platforms.MinecraftReceiver

class ChatHandlerRegistrationEvent(val configuration: Map<String, String>, val minecraftReceiver: MinecraftReceiver) :
    Event() {
    override fun getHandlers(): HandlerList = handlerList

    var chatHandler: ChatHandler? = null

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}