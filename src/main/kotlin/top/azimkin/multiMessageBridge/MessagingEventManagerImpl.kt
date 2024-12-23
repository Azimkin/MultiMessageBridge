package top.azimkin.multiMessageBridge

import org.bukkit.Bukkit
import top.azimkin.multiMessageBridge.api.events.AsyncChatMessageDispatchedEvent
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
import top.azimkin.multiMessageBridge.platforms.handlers.AdvancementHandler
import top.azimkin.multiMessageBridge.platforms.handlers.MessageHandler
import top.azimkin.multiMessageBridge.platforms.handlers.PlayerLifeHandler
import top.azimkin.multiMessageBridge.platforms.handlers.ServerInfoHandler
import top.azimkin.multiMessageBridge.platforms.handlers.ServerSessionHandler
import top.azimkin.multiMessageBridge.platforms.handlers.SessionHandler
import java.util.LinkedList
import java.util.concurrent.CompletableFuture.runAsync

class MessagingEventManagerImpl : MessagingEventManager {
    private val messageDispatchers: MutableList<MessageDispatcher> = LinkedList()
    private val sessionDispatchers: MutableList<SessionDispatcher> = LinkedList()
    private val serverSessionDispatchers: MutableList<ServerSessionDispatcher> = LinkedList()
    private val playerLifeDispatchers: MutableList<PlayerLifeDispatcher> = LinkedList()
    private val consoleDispatchers: MutableList<ConsoleMessageDispatcher> = LinkedList()
    private val advancementDispatchers: MutableList<AdvancementDispatcher> = LinkedList()

    private val messageHandlers: MutableList<MessageHandler> = LinkedList()
    private val sessionHandlers: MutableList<SessionHandler> = LinkedList()
    private val serverSessionHandlers: MutableList<ServerSessionHandler> = LinkedList()
    private val playerLifeHandlers: MutableList<PlayerLifeHandler> = LinkedList()
    private val serverInfoHandlers: MutableList<ServerInfoHandler> = LinkedList()

    private val advancementHandlers: MutableList<AdvancementHandler> = LinkedList()

    private val baseReceivers: MutableList<BaseReceiver> = LinkedList()
    override val receivers: List<BaseReceiver>
        get() = baseReceivers

    override fun dispatch(
        dispatcher: MessageDispatcher,
        context: MessageContext
    ) = runAsync {
        val event = AsyncChatMessageDispatchedEvent(context)
        val res = event.callEvent()
        if (!res) return@runAsync
        for (handler in messageHandlers) {
            if (handler == dispatcher) continue
            try {
                val receiver = handler as? BaseReceiver
                    ?: receivers.find { it == handler }
                    ?: throw Exception("BaseReceiver not found")
                handler.preHandle(event.context, receiver)
            } catch (e: Exception) {
                MultiMessageBridge.inst.logger.warning("Unable to send message context in ${handler.javaClass.name}")
                e.printStackTrace()
            }
        }
    }.let { }

    override fun dispatch(
        dispatcher: SessionDispatcher,
        context: SessionContext
    ) = runAsync {
        for (handler in sessionHandlers) {
            if (handler == dispatcher) continue
            try {
                handler.handle(context)
            } catch (e: Exception) {
                MultiMessageBridge.inst.logger.warning("Unable to send session context in ${handler.javaClass.name}")
                e.printStackTrace()
            }
        }
    }.let { }

    override fun dispatch(
        dispatcher: ServerSessionDispatcher,
        context: ServerSessionContext
    ) = runAsync {
        for (handler in serverSessionHandlers) {
            if (handler == dispatcher) continue
            try {
                handler.handle(context)
            } catch (e: Exception) {
                e.printStackTrace()
                MultiMessageBridge.inst.logger.warning("Unable to send serverSessionContext in ${handler.javaClass.name}")
            }
        }
    }.let { }

    override fun dispatch(
        dispatcher: PlayerLifeDispatcher,
        context: PlayerLifeContext
    ) = runAsync {
        for (handler in playerLifeHandlers) {
            if (handler == dispatcher) continue
            try {
                handler.handle(context)
            } catch (e: Exception) {
                e.printStackTrace()
                MultiMessageBridge.inst.logger.warning("Unable to send lifeContext in ${handler.javaClass.name}")
            }
        }
    }.let { }

    override fun dispatch(
        dispatcher: ConsoleMessageDispatcher,
        context: ConsoleMessageContext
    ) {
        Bukkit.getScheduler().runTask(MultiMessageBridge.inst) { _ ->
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), context.message)
        }
    }

    override fun dispatch(
        dispatcher: AdvancementDispatcher,
        context: AdvancementContext
    ) = runAsync {
        for (handler in advancementHandlers) {
            if (handler == dispatcher) continue
            try {
                handler.handle(context)
            } catch (e: Exception) {
                e.printStackTrace()
                MultiMessageBridge.inst.logger.warning("Unable to send advancementContext in ${handler.javaClass.name}")
            }
        }
    }.let {  }

    override fun dispatch(context: ServerInfoContext) {
        for (handler in serverInfoHandlers) {
            handler.handle(context)
        }
    }

    override fun register(vararg managers: BaseReceiver) {
        for (manager in managers) {
            try {
                baseReceivers.add(manager)
                if (manager is MessageHandler) {
                    messageHandlers.add(manager)
                }
                if (manager is PlayerLifeHandler) {
                    playerLifeHandlers.add(manager)
                }
                if (manager is ServerSessionHandler) {
                    serverSessionHandlers.add(manager)
                }
                if (manager is SessionHandler) {
                    sessionHandlers.add(manager)
                }
                if (manager is ServerInfoHandler) {
                    serverInfoHandlers.add(manager)
                }
                if (manager is AdvancementHandler) {
                    advancementHandlers.add(manager)
                }

                if (manager is MessageDispatcher) {
                    messageDispatchers.add(manager)
                }
                if (manager is PlayerLifeDispatcher) {
                    playerLifeDispatchers.add(manager)
                }
                if (manager is ServerSessionDispatcher) {
                    serverSessionDispatchers.add(manager)
                }
                if (manager is SessionDispatcher) {
                    sessionDispatchers.add(manager)
                }
                if (manager is ConsoleMessageDispatcher) {
                    consoleDispatchers.add(manager)
                }
                if (manager is AdvancementDispatcher) {
                    advancementDispatchers.add(manager)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun reloadAll() {
        receivers.forEach { it.reload() }
    }

    override fun reload(name: String) {
        receivers.forEach { if (it.name == name) it.reload() }
    }

    companion object {
        val instance = MessagingEventManagerImpl()
    }
}