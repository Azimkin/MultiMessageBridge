package top.azimkin.multiMessageBridge.platforms.discord

import me.scarsz.jdaappender.ChannelLoggingHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import top.azimkin.multiMessageBridge.server.ServerInfoProvider
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.api.events.AsyncDiscordMessageEvent
import top.azimkin.multiMessageBridge.api.events.JdaProviderRegistrationEvent
import top.azimkin.multiMessageBridge.configuration.ChannelConfiguration
import top.azimkin.multiMessageBridge.configuration.DiscordReceiverConfig
import top.azimkin.multiMessageBridge.data.ConsoleMessageContext
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.data.PlayerLifeContext
import top.azimkin.multiMessageBridge.data.ServerInfoContext
import top.azimkin.multiMessageBridge.data.ServerSessionContext
import top.azimkin.multiMessageBridge.data.SessionContext
import top.azimkin.multiMessageBridge.platforms.ConfigurableReceiver
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.CommonJdaProvider
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.JdaProvider
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.JdaProviderManager
import top.azimkin.multiMessageBridge.platforms.dispatchers.ConsoleMessageDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.MessageDispatcher
import top.azimkin.multiMessageBridge.platforms.handlers.MessageHandler
import top.azimkin.multiMessageBridge.platforms.handlers.PlayerLifeHandler
import top.azimkin.multiMessageBridge.platforms.handlers.ServerInfoHandler
import top.azimkin.multiMessageBridge.platforms.handlers.ServerSessionHandler
import top.azimkin.multiMessageBridge.platforms.handlers.SessionHandler
import top.azimkin.multiMessageBridge.utilities.format
import top.azimkin.multiMessageBridge.utilities.parseColor
import java.awt.Color
import kotlin.collections.ArrayDeque

class DiscordReceiver : ConfigurableReceiver<DiscordReceiverConfig>("Discord", DiscordReceiverConfig::class.java),
    MessageHandler, MessageDispatcher, PlayerLifeHandler, SessionHandler, ServerSessionHandler,
    ConsoleMessageDispatcher, ServerInfoHandler {
    private val executeQueue = ArrayDeque<() -> Unit>()
    private lateinit var console: ChannelLoggingHandler

    var jda: JdaProvider = createJdaProvider().apply {
        MultiMessageBridge.inst.logger.info("Using ${this.javaClass.name.split('.').last()} as jdaProvider")
        addInitializeListener(this@DiscordReceiver::attachConsole)
        addInitializeListener {
            while (executeQueue.isNotEmpty()) {
                executeQueue.removeFirst()()
            }
        }
    }

    override fun handle(context: MessageContext) {
        val base = getMessageOrBase(context.platform)
        val preparedMessage = base.format(
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
            messageConfig.format.format(
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
            message.format(
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

    @JvmOverloads
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

    @JvmOverloads
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
                dispatch(ConsoleMessageContext(event.message.contentRaw))
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
            add("default", CommonJdaProvider::class.java)
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
        val providerClass = manager.providers[providerName] ?: return useCommon("Unknown provider $providerName")

        if (!JdaProvider::class.java.isAssignableFrom(providerClass)) {
            return useCommon("$providerName must implement JdaProvider interface!")
        }

        return try {
            val constructors = providerClass.declaredConstructors
            val matchingConstructor = constructors.find { constructor ->
                val params = constructor.parameterTypes
                when (params.size) {
                    0 -> true
                    1 -> params[0] == String::class.java
                    2 -> params[0] == String::class.java && params[1] == DiscordReceiver::class.java
                    else -> false
                }
            } ?: throw IllegalArgumentException("No suitable constructor found for $providerName")

            val instance = when (matchingConstructor.parameterCount) {
                0 -> matchingConstructor.newInstance()
                1 -> matchingConstructor.newInstance(token)
                2 -> matchingConstructor.newInstance(token, this)
                else -> throw IllegalStateException("Unexpected constructor parameter count")
            }
            instance as JdaProvider
        } catch (e: Throwable) {
            e.printStackTrace()
            useCommon("$providerName encountered an exception during instantiation!")
        }
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
}