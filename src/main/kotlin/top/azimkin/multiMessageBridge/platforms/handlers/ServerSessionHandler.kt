package top.azimkin.multiMessageBridge.platforms.handlers

import top.azimkin.multiMessageBridge.data.ServerSessionContext
import top.azimkin.multiMessageBridge.platforms.dispatchers.BaseDispatcher

interface ServerSessionHandler : BaseDispatcher {
    fun handle(context: ServerSessionContext)
}