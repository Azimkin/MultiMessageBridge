package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MessagingEventManager
import top.azimkin.multiMessageBridge.data.ConsoleMessageContext

interface ConsoleMessageDispatcher : BaseDispatcher {
    fun dispatch(context: ConsoleMessageContext) {
        MessagingEventManager.get().dispatch(this, context)
    }
}