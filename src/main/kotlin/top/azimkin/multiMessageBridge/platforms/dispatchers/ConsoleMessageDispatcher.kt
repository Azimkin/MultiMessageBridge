package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.data.ConsoleMessageContext

interface ConsoleMessageDispatcher : BaseDispatcher {
    fun dispatch(context: ConsoleMessageContext) {
        MultiMessageBridge.inst.messagingEventManager.dispatch(this, context)
    }
}