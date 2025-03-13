package top.azimkin.multiMessageBridge.handlers.chat

import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.utilities.runBukkitAsync

abstract class ChatHandler {
    protected val listeners = mutableListOf<(MessageContext) -> Unit>()
    fun addListener(onMessage: (MessageContext) -> Unit): Unit = listeners.add(onMessage).let { }

    fun clear(): Unit = listeners.clear()

    fun onReceive(context: MessageContext) {
        runBukkitAsync { listeners.forEach { it(context) } }
    }
}