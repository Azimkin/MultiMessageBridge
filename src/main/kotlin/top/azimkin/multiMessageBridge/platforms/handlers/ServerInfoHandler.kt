package top.azimkin.multiMessageBridge.platforms.handlers

import top.azimkin.multiMessageBridge.data.ServerInfoContext

interface ServerInfoHandler : BaseHandler {
    fun handle(context: ServerInfoContext)
}