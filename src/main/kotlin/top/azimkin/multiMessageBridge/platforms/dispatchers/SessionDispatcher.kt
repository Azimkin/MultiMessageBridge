package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.data.SessionContext

interface SessionDispatcher : BaseDispatcher {
    fun dispatch(context: SessionContext) {
        MultiMessageBridge.inst.messagingEventManager.dispatch(this, context)
    }
}