package top.azimkin.multiMessageBridge.platforms.discord

import me.scarsz.jdaappender.ChannelLoggingHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import top.azimkin.multiMessageBridge.MessagingEventManager
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.api.events.AsyncDiscordMessageEvent
import top.azimkin.multiMessageBridge.api.events.JdaProviderRegistrationEvent
import top.azimkin.multiMessageBridge.configuration.ChannelConfiguration
import top.azimkin.multiMessageBridge.configuration.DiscordReceiverConfig
import top.azimkin.multiMessageBridge.data.*
import top.azimkin.multiMessageBridge.platforms.ConfigurableReceiver
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.CommonJdaProvider
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.JdaProvider
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.JdaProviderManager
import top.azimkin.multiMessageBridge.platforms.dispatchers.ConsoleMessageDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.MessageDispatcher
import top.azimkin.multiMessageBridge.platforms.handlers.*
import top.azimkin.multiMessageBridge.server.ServerInfoProvider
import top.azimkin.multiMessageBridge.utilities.formatByMap
import top.azimkin.multiMessageBridge.utilities.parseColor
import java.awt.Color

class DiscordReceiver(val em: MessagingEventManager) :
    ConfigurableReceiver<DiscordReceiverConfig>("Discord", DiscordReceiverConfig::class.java),
    MessageHandler, MessageDispatcher, PlayerLifeHandler, SessionHandler, ServerSessionHandler,
    ConsoleMessageDispatcher, ServerInfoHandler, AdvancementHandler {
    private val executeQueue = ArrayDeque<() -> Unit>()
    private lateinit var console: ChannelLoggingHandler

    val jda: JdaProvider = createJdaProvider().apply {
        logger.info("Using ${this.javaClass.simpleName} as jdaProvider")
        addInitializeListener(this@DiscordReceiver::attachConsole)
        addInitializeListener {
            while (executeQueue.isNotEmpty()) {
                executeQueue.removeFirst()()
            }
        }
    }

    override fun onDisable() {
        jda.shutdown()
        super.onDisable()
    }

    override fun handle(context: MessageContext) {
        val base = getMessageOrBase(context.platform)
        val preparedMessage = base.formatByMap(
            mapOf(
                "role" to (context.role ?: ""),
                "nickname" to context.senderName,
                "platform" to context.platform,
                "message" to context.message,
                "old_message" to (context.reply ?: ""),
                "reply_user" to (context.replyUser ?: ""),
            )
        )
        sendMessageToChannel(
            preparedMessage.replace("@everyone", "евериване", true)
                .replace("@here", "хере", true)
        )
    }

    override fun handle(context: PlayerLifeContext) {
        val messageConfig = config.messages.death
        sendSimpleEmbed(
            context.playerName,
            messageConfig.format.formatByMap(
                mapOf(
                    "nickname" to context.playerName,
                    "death_message" to context.deathSource
                )
            ),
            parseColor(messageConfig.configuration["color"])
        )
    }

    override fun handle(context: SessionContext) {
        var message = ""
        var color: Color = Color.BLACK
        if (context.isJoined) {
            if (context.isFirstJoined) {
                val cfg = config.messages.firstJoin
                message = cfg.format
                color = parseColor(cfg.configuration["color"])
            } else {
                val cfg = config.messages.join
                message = cfg.format
                color = parseColor(cfg.configuration["color"])
            }
        } else {
            val cfg = config.messages.leave
            message = cfg.format
            color = parseColor(cfg.configuration["color"])
        }

        sendSimpleEmbed(
            context.playerName,
            message.formatByMap(
                mapOf(
                    "nickname" to context.playerName,
                    "is_joined" to context.isJoined.toString(),
                    "is_first_join" to context.isFirstJoined.toString()
                )
            ), color
        )
    }

    override fun handle(context: ServerSessionContext) {
        if (context.isTurnedOn) {
            sendMessageToChannel(config.messages.serverEnabled.format)
        } else {
            sendMessageToChannel(config.messages.serverDisabled.format)

        }
    }

    fun sendSimpleEmbed(author: String, message: String, color: Color = Color.BLACK, channel: String = "main_text") =
        addAction {
            val channel = findChannel(channel)?.id ?: return@addAction
            val textChannel = jda.get().getTextChannelById(channel) ?: return@addAction
            val headUrl = MultiMessageBridge.inst.headProvider.getHeadUrl(author)
            textChannel.sendMessageEmbeds(
                EmbedBuilder()
                    .setAuthor(message, null, headUrl)
                    .setColor(color)
                    .build()
            ).queue()
        }

    fun sendMessageToChannel(message: String, channel: String = "main_text") = addAction {
        val channel = findChannel(channel)?.id ?: return@addAction
        val textChannel = jda.get().getTextChannelById(channel) ?: return@addAction
        textChannel
            .sendMessage(message)
            .queue()
    }

    fun dispatchMessage(event: MessageReceivedEvent) {
        val bukkitEvent = AsyncDiscordMessageEvent(event, this)
        if (!bukkitEvent.callEvent()) return
        if (event.author.isBot && bukkitEvent.mustBeCanceledIfBot) return
        //MultiMessageBridge.inst.logger.info(event.toString())
        val channelConfig = findChannel(id = event.channel.idLong) ?: return
        when (channelConfig.type) {
            "main_text" -> {
                val context = MessageContext(
                    event.member?.effectiveName ?: event.author.name,
                    event.message.contentDisplay,
                    event.message.referencedMessage != null,
                    name,
                    event.message.referencedMessage?.contentRaw,
                    event.message.referencedMessage?.member?.effectiveName
                        ?: event.message.referencedMessage?.author?.name,
                    event.member?.roles?.getOrNull(0)?.name,
                    roleColor = event.member?.roles?.getOrNull(0)?.color,
                    urlAttachments = event.message.attachments.map { it.url },
                )
                dispatch(context)
            }

            "console" -> {
                console.dumpStack()
                if (config.bot.commandsShouldStartsWithPrefix && !event.message.contentRaw.startsWith(config.bot.commandPrefix)) return
                dispatch(ConsoleMessageContext(event.message.contentRaw.substring(config.bot.commandPrefix.length)))
            }
        }
    }

    private fun getMessageOrBase(platform: String): String =
        config.messages.customFormats[platform]?.format
            ?: config.messages.messageBase.format

    private fun addAction(act: () -> Unit) {
        if (jda.isInitialized()) {
            act()
        } else {
            executeQueue.add(act)
        }
    }

    private fun createJdaProvider(): JdaProvider {
        val token = config.bot.token
        val manager = JdaProviderManager().apply {
            add("default") { token, receiver -> CommonJdaProvider(token, receiver) }
        }
        JdaProviderRegistrationEvent(manager).callEvent()

        fun useCommon(error: String? = null): JdaProvider {
            error?.let { MultiMessageBridge.inst.logger.warning("Using common provider as JDA provider | $it") }
            return CommonJdaProvider(token, this)
        }

        if (manager.providers.isEmpty()) {
            return useCommon("No available JDA providers found!")
        }
        val providerName = config.advanced.jdaProvider

        return manager.providers[providerName]?.invoke(token, this) ?: useCommon("Unknown provider $providerName")
    }

    private fun attachConsole(jda: JDA) {
        MultiMessageBridge.inst.logger.info("Attaching console appender")
        console =
            ChannelLoggingHandler({ jda.getTextChannelById(findChannel("console")?.id ?: 0) }) { config ->
                config.isColored = true
                config.mapLoggerName("net.dv8tion.jda.api.JDA", "JDA")
                config.mapLoggerName("net.dv8tion.jda", "JDA")
                config.mapLoggerName("net.minecraft.top.azimkin.multiMessageBridge.server.MinecraftServer", "Server")
                config.mapLoggerNameFriendly("net.minecraft.top.azimkin.multiMessageBridge.server") { s -> "Server/$s" }
                config.mapLoggerNameFriendly("net.minecraft") { s -> "Minecraft/$s" }
            }.attachLog4jLogging().schedule()
        MultiMessageBridge.inst.logger.info("ConsoleAppender must be attached")
    }

    fun findChannel(type: String? = null, id: Long? = null): ChannelConfiguration? =
        config.bot.channels.values.find { it.type == type || it.id == id }

    override fun handle(context: ServerInfoContext) = addAction {
        for ((_, j) in config.bot.channels) {
            jda.get().getTextChannelById(j.id)?.manager
                ?.setTopic(ServerInfoProvider.parse(if (j.description == "") context.text else j.description))
                ?.queue()
        }
    }

    override fun handle(context: AdvancementContext) {
        sendMessageToChannel(
            config.messages.advancementGrant.format.formatByMap(
                mapOf(
                    "nickname" to context.playerName,
                    "advancement" to context.advancementName,
                    "description" to context.description,
                    "rarity" to context.rarity.name,
                )
            )
        )
    }
}