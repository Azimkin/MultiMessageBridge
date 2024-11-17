package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MessagingEventManager
import top.azimkin.multiMessageBridge.data.MessageContext

interface MessageDispatcher : BaseDispatcher {
    fun dispatch(context: MessageContext) {
        MessagingEventManager.get().dispatch(this, context)
    }
}