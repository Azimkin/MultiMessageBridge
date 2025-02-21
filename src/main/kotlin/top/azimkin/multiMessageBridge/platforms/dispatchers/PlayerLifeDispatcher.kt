package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.data.PlayerLifeContext

interface PlayerLifeDispatcher : BaseDispatcher {
    fun dispatch(context: PlayerLifeContext) {
        MultiMessageBridge.inst.messagingEventManager.dispatch(this, context)
    }
}