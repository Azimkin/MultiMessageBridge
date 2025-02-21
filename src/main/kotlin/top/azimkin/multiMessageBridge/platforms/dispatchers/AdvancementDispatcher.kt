package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.data.AdvancementContext

interface AdvancementDispatcher : BaseDispatcher {
    fun dispatch(context: AdvancementContext) {
        MultiMessageBridge.inst.messagingEventManager.dispatch(this, context)
    }
}