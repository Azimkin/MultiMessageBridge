package top.azimkin.multiMessageBridge.platforms.handlers

import top.azimkin.multiMessageBridge.data.AdvancementContext

interface AdvancementHandler : BaseHandler {
    fun handle(context: AdvancementContext)
}