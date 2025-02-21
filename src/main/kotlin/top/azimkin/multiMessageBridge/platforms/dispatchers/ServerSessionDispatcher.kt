package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.data.ServerSessionContext

interface ServerSessionDispatcher : BaseDispatcher {
    fun dispatch(context: ServerSessionContext) {
        MultiMessageBridge.inst.messagingEventManager.dispatch(this, context)
    }
}