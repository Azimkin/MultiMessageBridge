package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MessagingEventManager
import top.azimkin.multiMessageBridge.data.SessionContext

interface SessionDispatcher : BaseDispatcher {
    fun dispatch(context: SessionContext) {
        MessagingEventManager.get().dispatch(this, context)
    }
}