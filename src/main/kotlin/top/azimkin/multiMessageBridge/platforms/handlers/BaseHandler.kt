package top.azimkin.multiMessageBridge.platforms.handlers

import top.azimkin.multiMessageBridge.data.BaseContext

interface BaseHandler {
    fun handle(context: BaseContext)
}