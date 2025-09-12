package top.azimkin.multiMessageBridge.platforms.handlers

import top.azimkin.multiMessageBridge.api.events.AsyncChatMessageReceivedEvent
import top.azimkin.multiMessageBridge.data.BaseContext
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.platforms.BaseReceiver

interface MessageHandler : BaseHandler {
    override fun handle(context: BaseContext) {
        if (context is MessageContext) handle(context)
    }

    fun handle(context: MessageContext): Long?

    fun preHandle(context: MessageContext, dispatcher: BaseReceiver): Long? {
        val event = AsyncChatMessageReceivedEvent(context, dispatcher)
        val res = event.callEvent()
        if (res) return handle(event.context)
        return null
    }
}