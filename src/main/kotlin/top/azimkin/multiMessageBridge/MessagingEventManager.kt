package top.azimkin.multiMessageBridge

import top.azimkin.multiMessageBridge.data.AdvancementContext
import top.azimkin.multiMessageBridge.data.ConsoleMessageContext
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.data.PlayerLifeContext
import top.azimkin.multiMessageBridge.data.ServerInfoContext
import top.azimkin.multiMessageBridge.data.ServerSessionContext
import top.azimkin.multiMessageBridge.data.SessionContext
import top.azimkin.multiMessageBridge.platforms.BaseReceiver
import top.azimkin.multiMessageBridge.platforms.dispatchers.AdvancementDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.ConsoleMessageDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.MessageDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.PlayerLifeDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.ServerSessionDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.SessionDispatcher

interface MessagingEventManager {
    val receivers: List<BaseReceiver>

    fun dispatch(dispatcher: MessageDispatcher, context: MessageContext)

    fun dispatch(dispatcher: SessionDispatcher, context: SessionContext)

    fun dispatch(dispatcher: ServerSessionDispatcher, context: ServerSessionContext)

    fun dispatch(dispatcher: PlayerLifeDispatcher, context: PlayerLifeContext)

    fun dispatch(dispatcher: ConsoleMessageDispatcher, context: ConsoleMessageContext)

    fun dispatch(dispatcher: AdvancementDispatcher, context: AdvancementContext)

    fun dispatch(context: ServerInfoContext)

    fun register(vararg managers: BaseReceiver)

    fun reloadAll()

    fun reload(name: String)

    companion object {
        @JvmStatic
        fun get(): MessagingEventManager = MessagingEventManagerImpl.instance
    }
}