package top.azimkin.multiMessageBridge.platforms.handlers

import top.azimkin.multiMessageBridge.data.SessionContext
import top.azimkin.multiMessageBridge.platforms.dispatchers.BaseDispatcher

interface SessionHandler : BaseDispatcher {
    fun handle(context: SessionContext)
}