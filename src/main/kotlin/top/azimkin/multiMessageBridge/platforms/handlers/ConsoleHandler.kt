package top.azimkin.multiMessageBridge.platforms.handlers

import top.azimkin.multiMessageBridge.data.ConsoleMessageContext

interface ConsoleHandler : BaseHandler {
    fun handle(context: ConsoleMessageContext)
}