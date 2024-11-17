package top.azimkin.multiMessageBridge.platforms.handlers

import top.azimkin.multiMessageBridge.api.events.AsyncChatMessageReceivedEvent
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.platforms.BaseReceiver
import top.azimkin.multiMessageBridge.platforms.dispatchers.BaseDispatcher

interface MessageHandler : BaseDispatcher {
    fun handle(context: MessageContext)

    fun preHandle(context: MessageContext, dispatcher: BaseReceiver) {
        val event = AsyncChatMessageReceivedEvent(context, dispatcher)
        val res = event.callEvent()
        if (res) handle(event.context)
    }
}