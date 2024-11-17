package top.azimkin.multiMessageBridge.platforms.handlers

import top.azimkin.multiMessageBridge.data.PlayerLifeContext
import top.azimkin.multiMessageBridge.platforms.dispatchers.BaseDispatcher

interface PlayerLifeHandler : BaseDispatcher {
    fun handle(context: PlayerLifeContext)
}