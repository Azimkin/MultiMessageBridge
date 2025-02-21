package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.data.MessageContext

interface MessageDispatcher : BaseDispatcher {
    fun dispatch(context: MessageContext) {
        MultiMessageBridge.inst.messagingEventManager.dispatch(this, context)
    }
}