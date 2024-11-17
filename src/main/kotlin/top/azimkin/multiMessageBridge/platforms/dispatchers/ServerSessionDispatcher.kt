package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MessagingEventManager
import top.azimkin.multiMessageBridge.data.ServerSessionContext

interface ServerSessionDispatcher : BaseDispatcher {
    fun dispatch(context: ServerSessionContext) {
        MessagingEventManager.get().dispatch(this, context)
    }
}