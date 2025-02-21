package top.azimkin.multiMessageBridge

import top.azimkin.multiMessageBridge.data.*
import top.azimkin.multiMessageBridge.platforms.BaseReceiver
import top.azimkin.multiMessageBridge.platforms.dispatchers.*

interface MessagingEventManager {
    val receivers: List<BaseReceiver>

    fun dispatch(dispatcher: MessageDispatcher, context: MessageContext)

    fun dispatch(dispatcher: SessionDispatcher, context: SessionContext)

    fun dispatch(dispatcher: ServerSessionDispatcher, context: ServerSessionContext)

    fun dispatch(dispatcher: PlayerLifeDispatcher, context: PlayerLifeContext)

    fun dispatch(dispatcher: ConsoleMessageDispatcher, context: ConsoleMessageContext)

    fun dispatch(dispatcher: AdvancementDispatcher, context: AdvancementContext)

    fun dispatch(context: ServerInfoContext)

    fun register(vararg receivers: Pair<String, () -> BaseReceiver>)

    fun enable(name: String)

    fun enable(enabled: List<String>)

    fun enableAll()

    fun reloadAll()

    fun reload(name: String)

    fun disable(name: String)
}