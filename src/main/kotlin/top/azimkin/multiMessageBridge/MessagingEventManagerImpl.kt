package top.azimkin.multiMessageBridge

import com.j256.ormlite.dao.ForeignCollection
import org.bukkit.Bukkit
import org.slf4j.LoggerFactory
import top.azimkin.multiMessageBridge.api.events.AsyncChatMessageDispatchedEvent
import top.azimkin.multiMessageBridge.api.events.ReceiverEnabledEvent
import top.azimkin.multiMessageBridge.data.*
import top.azimkin.multiMessageBridge.entities.CrossPlatformMessage
import top.azimkin.multiMessageBridge.entities.MessagePlatformMapping
import top.azimkin.multiMessageBridge.platforms.BaseReceiver
import top.azimkin.multiMessageBridge.platforms.dispatchers.*
import top.azimkin.multiMessageBridge.platforms.handlers.*
import top.azimkin.multiMessageBridge.services.MessageService
import top.azimkin.multiMessageBridge.services.imagehosting.ImageHosting
import top.azimkin.multiMessageBridge.providers.username.UsernameProvider
import java.util.concurrent.CompletableFuture.runAsync

class MessagingEventManagerImpl(
    val messageService: MessageService,
    val usernameProvider: UsernameProvider,
    val imageHosting: ImageHosting
) : MessagingEventManager {
    private val logger = LoggerFactory.getLogger("MessagingEventManagerImpl")

    private val baseReceivers = HashMap<String, BaseReceiver>()
    override val receivers: List<BaseReceiver>
        get() = baseReceivers.values.toList()

    private val registeredReceivers = HashMap<String, () -> BaseReceiver>()

    override fun dispatch(
        dispatcher: MessageDispatcher,
        context: MessageContext
    ) = runAsync {
        val event = AsyncChatMessageDispatchedEvent(context)
        val res = event.callEvent()
        if (!res) return@runAsync

        if(context.message == null &&
            context.sticker == null &&
            context.images.isEmpty())
            return@runAsync

        val username = context.senderPlatformId?.let {
            usernameProvider.getUsername(it, context.platform)
        } ?: context.senderName

        var replyMessage: CrossPlatformMessage? = null
        val replyToId = context.replyId?.let{
            replyMessage = messageService.findMessageByPlatformId(it, context.platform)
            replyMessage?.id
        }

        val imageUrls = mutableListOf<String>()
        for (imageData in context.images) {
            imageHosting.uploadImage(imageData)?.let{
                imageUrls.add(it)
            }
        }

        context.images = imageUrls

        val handlingMessage = messageService.createNewMessage(
            authorUsername = username,
            text = context.message ?: "",
            sticker = context.sticker,
            replyToId=replyToId
        )!!


        messageService.addPlatformMapping(handlingMessage,
            context.platform,
            context.messagePlatformId!!,
            context.message ?: ""
        )

        context.message = context.message ?: ""

        var mappingCollection: ForeignCollection<MessagePlatformMapping>? = null
        if(replyMessage != null) {
            mappingCollection = replyMessage.mappings
            context.replyUser = replyMessage.authorUsername
            context.replyText = replyMessage.text
        }
        eachTyped<MessageHandler>(dispatcher) { handler ->
            try {
                val receiver = handler as? BaseReceiver
                    ?: receivers.find { it == handler }
                    ?: throw Exception("BaseReceiver not found")

                MultiMessageBridge.inst.logger.info("${context.message}: ${receiver.name}")

                context.replyId = null
                if (replyMessage != null && mappingCollection != null){
                    for (item in mappingCollection) {
                        if (item.platform == receiver.name)
                            context.replyId = item.platformMessageId
                        if (item.platform == "Minecraft"){
                            context.replyText = item.platformMessageText
                        }
                    }
                }

                val platformId = handler.preHandle(event.context, receiver)

                messageService.addPlatformMapping(
                    handlingMessage,
                    receiver.name,
                    platformId,
                    context.message ?: "")

            } catch (e: Exception) {
                MultiMessageBridge.inst.logger.warning("Unable to send message context in ${handler.javaClass.name}")
                e.printStackTrace()
            }
        }
    }.let { }

    @OptIn(ExperimentalStdlibApi::class)
    private inline fun <reified T> eachTyped(filter: Any, action: (T) -> Unit) {
        for (smth in baseReceivers.values) {
            if (smth == filter || smth !is T) continue
            action.invoke(smth)
        }
    }

    override fun dispatch(
        dispatcher: SessionDispatcher,
        context: SessionContext
    ) = runAsync {
        eachTyped<SessionHandler>(dispatcher) { handler ->
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
    ) {
        eachTyped<ServerSessionHandler>(dispatcher) { handler ->
            try {
                handler.handle(context)
            } catch (e: Exception) {
                e.printStackTrace()
                MultiMessageBridge.inst.logger.warning("Unable to send serverSessionContext in ${handler.javaClass.name}")
            }
        }
    }

    override fun dispatch(
        dispatcher: PlayerLifeDispatcher,
        context: PlayerLifeContext
    ) = runAsync {
        eachTyped<PlayerLifeHandler>(dispatcher) { handler ->
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
        eachTyped<AdvancementHandler>(dispatcher) { handler ->
            try {
                handler.handle(context)
            } catch (e: Exception) {
                e.printStackTrace()
                MultiMessageBridge.inst.logger.warning("Unable to send advancementContext in ${handler.javaClass.name}")
            }
        }
    }.let { }

    override fun dispatch(context: ServerInfoContext) {
        eachTyped<ServerInfoHandler>(0) { handler ->
            handler.handle(context)
        }
    }

    override fun enable(name: String) {
        try {
            val manager = registeredReceivers[name]?.invoke() ?: run {
                logger.warn("Unknown receiver $name")
                return
            }
            baseReceivers.put(name, manager)
            ReceiverEnabledEvent(manager).callEvent()
            logger.info("${manager.name} enabled")
        } catch (e: Throwable) {
            logger.error("Unable to enable receiver $name", e)
        }
    }

    override fun enable(enabled: List<String>) {
        if (enabled.isEmpty()) enableAll()
        else if (enabled[0] == "DISABLED") {
            logger.info("Plugin disabled fr")
            return
        }
        logger.info("RegisteredReceivers: ")
        var c = 1;
        for ((i, _) in registeredReceivers) {
            logger.info("${c++}. $i")
        }
        for (i in enabled) {
            enable(i)
        }
    }

    override fun enableAll() {
        enable(registeredReceivers.keys.toList())
    }

    override fun register(vararg receivers: Pair<String, () -> BaseReceiver>) {
        registeredReceivers.putAll(receivers)
    }

    override fun reloadAll() {
        receivers.forEach { it.reload() }
    }

    override fun reload(name: String) {
        receivers.forEach { if (it.name == name) it.reload() }
    }

    override fun disable(name: String) {
        val receiver = baseReceivers[name] ?: return
        receiver.onDisable()
        baseReceivers.remove(name)
    }
}