package top.azimkin.multiMessageBridge.platforms.discord

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import top.azimkin.multiMessageBridge.api.events.AsyncDiscordMessageEvent
import top.azimkin.multiMessageBridge.configuration.ChannelConfiguration
import top.azimkin.multiMessageBridge.configuration.DiscordReceiverConfig
import top.azimkin.multiMessageBridge.configuration.MessageConfiguration
import top.azimkin.multiMessageBridge.data.*
import top.azimkin.multiMessageBridge.platforms.ConfigurableReceiver
import top.azimkin.multiMessageBridge.platforms.dispatchers.MessageDispatcher
import top.azimkin.multiMessageBridge.platforms.handlers.MessageHandler
import top.azimkin.multiMessageBridge.platforms.handlers.PlayerLifeHandler
import top.azimkin.multiMessageBridge.platforms.handlers.ServerSessionHandler
import top.azimkin.multiMessageBridge.platforms.handlers.SessionHandler
import top.azimkin.multiMessageBridge.utilities.formatByMap
import java.awt.Color
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

class Discord4jReceiver() : ConfigurableReceiver<DiscordReceiverConfig>("Discord4j", DiscordReceiverConfig::class.java),
    MessageHandler, MessageDispatcher, PlayerLifeHandler, SessionHandler, ServerSessionHandler {

    private lateinit var client: GatewayDiscordClient
    private val channelExecutors: HashMap<String, MutableSet<(MessageCreateEvent) -> Unit>> = hashMapOf()
    private val messageDispatchers: HashMap<String, (Long, ConfiguredMessage) -> Unit> = hashMapOf()

    init {
        reload()
    }

    override fun reload() {
        super.reload()
        client = DiscordClient.create(config.bot.token)
            .gateway()
            .setEnabledIntents(IntentSet.of(Intent.GUILD_MESSAGES, Intent.GUILD_MEMBERS, Intent.MESSAGE_CONTENT))
            .login()
            .block()!!
        client.apply {
            on(ReadyEvent::class.java).subscribe { event ->
                logger.info("Client ready!")
            }
            on(MessageCreateEvent::class.java).subscribe(this@Discord4jReceiver::onMessage)
        }
        addChannelExecutor("main_text", this@Discord4jReceiver::onTextMessage)
        addChannelExecutor("console", this@Discord4jReceiver::onConsoleMessage)
    }

    private fun onMessage(event: MessageCreateEvent) {
        val isBot = event.message.author.orElse(null)?.isBot == true
        val bukkitEvent = AsyncDiscordMessageEvent(event, this)
        if (!bukkitEvent.callEvent() || (isBot && bukkitEvent.mustBeCanceledIfBot) || event.message.author.getOrNull() == null) return
        var channelConfig = findChannel(id = event.message.channelId.asLong()) ?: return
        channelExecutors[channelConfig.type]?.forEach { it(event) }

    }

    private fun onTextMessage(event: MessageCreateEvent) {
        var message = event.message
        var author = message.author.get()
        dispatch(messageContext(this) {
            senderName = author.globalName.getOrElse { author.username }
            this.message = message.content
            message.referencedMessage.ifPresent { msg ->
                isReply = true
                msg.author.ifPresent { ra ->
                    replyUser = ra.globalName.getOrElse { ra.username }
                }
                reply = msg.content
            }
            var member = event.member.getOrNull() ?: return@messageContext
            roleColor = Color(member.roles.blockFirst()?.color?.rgb ?: 0)
        })
    }

    private fun onConsoleMessage(event: MessageCreateEvent) {
        //TODO
    }

    override fun onDisable() {
        client.logout().block()
        super.onDisable()
    }

    override fun handle(context: MessageContext) {
        //sendMessage(ConfiguredMessage.fromMessageContext(context), "main_text")
    }

    override fun handle(context: PlayerLifeContext) {
        TODO("Not yet implemented")
    }

    override fun handle(context: SessionContext) {
        TODO("Not yet implemented")
    }

    override fun handle(context: ServerSessionContext) {
        TODO("Not yet implemented")
    }

    private fun sendMessage(message: ConfiguredMessage, channel: String) {
        val dispatcher = messageDispatchers[message.config.type] ?: run {
            logger.warn("Unknown message dispatcher ${message.config.type}")
            return
        }
        val channel = findChannel(channel) ?: return
        dispatcher(channel.id, message)
    }

    private fun addChannelExecutor(id: String, action: (MessageCreateEvent) -> Unit) {
        val set = channelExecutors[id] ?: HashSet()
        set.add(action)
        channelExecutors[id] = set
    }

    fun findChannel(type: String? = null, id: Long? = null): ChannelConfiguration? =
        config.bot.channels.values.find { it.type == type || it.id == id }


    private class ConfiguredMessage(val params: Map<String, String>, val config: MessageConfiguration) {
        fun getParam(id: String) = params[id]
        val format: String; get() = config.format

        fun plainFormatted(): String {
            return format.formatByMap(params)
        }

        companion object {
            fun fromMessageContext(context: MessageContext, config: MessageConfiguration): ConfiguredMessage {
                return ConfiguredMessage(
                    mapOf(
                        "role" to (context.role ?: ""),
                        "nickname" to context.senderName,
                        "platform" to context.platform,
                        "message" to context.message,
                        "old_message" to (context.reply ?: ""),
                        "reply_user" to (context.replyUser ?: ""),
                    ),
                    config
                )
            }
        }
    }
}