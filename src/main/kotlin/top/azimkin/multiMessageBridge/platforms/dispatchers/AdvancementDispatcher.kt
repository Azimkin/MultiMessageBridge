package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MessagingEventManager
import top.azimkin.multiMessageBridge.data.AdvancementContext

interface AdvancementDispatcher : BaseDispatcher {
    fun dispatch(context: AdvancementContext) {
        MessagingEventManager.get().dispatch(this, context)
    }
}