package top.azimkin.multiMessageBridge.platforms.dispatchers

import top.azimkin.multiMessageBridge.MessagingEventManager
import top.azimkin.multiMessageBridge.data.PlayerLifeContext

interface PlayerLifeDispatcher : BaseDispatcher {
    fun dispatch(context: PlayerLifeContext) {
        MessagingEventManager.get().dispatch(this, context)
    }
}