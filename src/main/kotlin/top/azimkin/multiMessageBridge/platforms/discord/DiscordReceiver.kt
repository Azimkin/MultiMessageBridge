package top.azimkin.multiMessageBridge.platforms.discord

import me.scarsz.jdaappender.ChannelLoggingHandler
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.api.events.AsyncDiscordMessageEvent
import top.azimkin.multiMessageBridge.api.events.JdaProviderRegistrationEvent
import top.azimkin.multiMessageBridge.data.ConsoleMessageContext
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.data.PlayerLifeContext
import top.azimkin.multiMessageBridge.data.ServerSessionContext
import top.azimkin.multiMessageBridge.data.SessionContext
import top.azimkin.multiMessageBridge.platforms.BaseReceiver
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.CommonJdaProvider
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.JdaProvider
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.JdaProviderManager
import top.azimkin.multiMessageBridge.platforms.dispatchers.ConsoleMessageDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.MessageDispatcher
import top.azimkin.multiMessageBridge.platforms.handlers.MessageHandler
import top.azimkin.multiMessageBridge.platforms.handlers.PlayerLifeHandler
import top.azimkin.multiMessageBridge.platforms.handlers.ServerSessionHandler
import top.azimkin.multiMessageBridge.platforms.handlers.SessionHandler
import top.azimkin.multiMessageBridge.utilities.format
import top.azimkin.multiMessageBridge.utilities.parseColor
import java.awt.Color
import kotlin.collections.ArrayDeque

class DiscordReceiver : BaseReceiver(
    "Discord",
    mapOf(
        "jdaProvider" to "default",
        "channel" to 0L,
        "console_channel" to 0L,
        "guild_id" to 0L,
        "token" to "bot_token",
        "messages.format._base" to "<platform> <nickname> -> <message>",
        "messages.format.dead.message" to "<death_message>",
        "messages.format.dead.color" to "0:0:150",
        "messages.format.join.message" to "<nickname> has joined the game!",
        "messages.format.join.color" to "125:120:20",
        "messages.format.leave.message" to "<nickname> has left the game!",
        "messages.format.leave.color" to "50:50:100",
        "messages.format.firstJoin.message" to "<nickname> has joined first time!",
        "messages.format.firstJoin.color" to "255:255:50",
        "messages.format.server_enabled" to "Server enabled!",
        "messages.format.server_disabled" to "Server disabled!",
    )
), MessageHandler, MessageDispatcher, PlayerLifeHandler, SessionHandler, ServerSessionHandler,
    ConsoleMessageDispatcher {
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
        sendSimpleEmbed(
            context.playerName,
            config.getTranslation("messages.format.dead.message").format(
                mapOf(
                    "nickname" to context.playerName,
                    "death_message" to context.deathSource
                )
            ),
            parseColor(config.getString("messages.format.dead.color"))
        )
    }

    override fun handle(context: SessionContext) {
        var message = ""
        var color: Color = Color.BLACK
        if (context.isJoined) {
            if (context.isFirstJoined) {
                message = config.getTranslation("messages.format.firstJoin.message")
                color = parseColor(config.getString("messages.format.firstJoin.color"))
            } else {
                message = config.getTranslation("messages.format.join.message")
                color = parseColor(config.getString("messages.format.join.color"))
            }
        } else {
            message = config.getTranslation("messages.format.leave.message")
            color = parseColor(config.getString("messages.format.leave.color"))
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
            sendMessageToChannel(config.getTranslation("messages.format.server_enabled"))
        } else {
            sendMessageToChannel(config.getTranslation("messages.format.server_disabled"))
        }
    }

    @JvmOverloads
    fun sendSimpleEmbed(author: String, message: String, color: Color = Color.BLACK) = addAction {
        val channel = config.getLong("channel")
        val textChannel = jda.get().getTextChannelById(channel) ?: return@addAction
        val headUrl = MultiMessageBridge.inst.headProvider.getHeadUrl(author)
        textChannel.sendMessageEmbeds(
            EmbedBuilder()
                .setAuthor(author, null, headUrl)
                .setTitle(message)
                .setColor(color)
                .build()
        ).queue()

    }

    @JvmOverloads
    fun sendMessageToChannel(message: String, channel: Long = config.getLong("channel")) = addAction {
        val textChannel = jda.get().getTextChannelById(channel) ?: return@addAction
        textChannel
            .sendMessage(message)
            .queue()
    }

    fun dispatchMessage(event: MessageReceivedEvent) {
        val bukkitEvent = AsyncDiscordMessageEvent(event)
        if (!bukkitEvent.callEvent()) return
        if (event.author.isBot && bukkitEvent.mustBeCanceledIfBot) return
        //MultiMessageBridge.inst.logger.info(event.toString())
        if (event.channel.idLong == config.getLong("channel")) {
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
        } else if (event.channel.idLong == config.getLong("console_channel")) {
            console.dumpStack()
            dispatch(ConsoleMessageContext(event.message.contentRaw))
        }
    }

    private fun getMessageOrBase(platform: String): String =
        config.getString("messages.format.${platform}")
            ?: config.getString("messages.format._base")
            ?: "$platform <nickname> -> <message>"

    private fun addAction(act: () -> Unit) {
        if (jda.isInitialized()) {
            act()
        } else {
            executeQueue.add(act)
        }
    }

    private fun createJdaProvider(): JdaProvider {
        val token = config.getString("token") ?: "unknown"
        val manager = JdaProviderManager().apply {
            add("default" to CommonJdaProvider::class.java)
        }
        JdaProviderRegistrationEvent(manager).callEvent()

        fun useCommon(error: String? = null): JdaProvider {
            error?.let { MultiMessageBridge.inst.logger.warning("Using common provider as JDA provider | $it") }
            return CommonJdaProvider(token, this)
        }

        if (manager.providers.isEmpty) {
            return useCommon("No available JDA providers found!")
        }

        val providerName = config.getString("jdaProvider")
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
            ChannelLoggingHandler({ jda.getTextChannelById(config.getLong("console_channel")) }) { config ->
                config.isColored = true
                config.mapLoggerName("net.dv8tion.jda.api.JDA", "JDA")
                config.mapLoggerName("net.dv8tion.jda", "JDA")
                config.mapLoggerName("net.minecraft.server.MinecraftServer", "Server")
                config.mapLoggerNameFriendly("net.minecraft.server") { s -> "Server/$s" }
                config.mapLoggerNameFriendly("net.minecraft") { s -> "Minecraft/$s" }
            }.attachLog4jLogging().schedule()
        MultiMessageBridge.inst.logger.info("ConsoleAppender must be attached")
    }
}