package top.azimkin.multiMessageBridge.platforms.discord

import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.core.`object`.entity.channel.TextChannel
import discord4j.core.spec.EmbedCreateFields.Author
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.TextChannelEditSpec
import me.scarsz.jdaappender.ChannelLoggingHandler
import reactor.core.publisher.Mono
import top.azimkin.multiMessageBridge.MessagingEventManager
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.api.events.AsyncDiscordMessageEvent
import top.azimkin.multiMessageBridge.api.events.DiscordProviderRegistrationEvent
import top.azimkin.multiMessageBridge.configuration.ChannelConfiguration
import top.azimkin.multiMessageBridge.configuration.DiscordReceiverConfig
import top.azimkin.multiMessageBridge.data.*
import top.azimkin.multiMessageBridge.platforms.ConfigurableReceiver
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.CommonDiscordProvider
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.Discord4jProviderManager
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.DiscordProvider
import top.azimkin.multiMessageBridge.platforms.dispatchers.ConsoleMessageDispatcher
import top.azimkin.multiMessageBridge.platforms.dispatchers.MessageDispatcher
import top.azimkin.multiMessageBridge.platforms.handlers.*
import top.azimkin.multiMessageBridge.server.ServerInfoProvider
import top.azimkin.multiMessageBridge.utilities.formatByMap
import top.azimkin.multiMessageBridge.utilities.parseColor
import java.awt.Color
import kotlin.jvm.optionals.getOrNull

class DiscordReceiver(val em: MessagingEventManager) :
    ConfigurableReceiver<DiscordReceiverConfig>("Discord", DiscordReceiverConfig::class.java),
    MessageHandler, MessageDispatcher, PlayerLifeHandler, SessionHandler, ServerSessionHandler,
    ConsoleMessageDispatcher, ServerInfoHandler, AdvancementHandler {
    private val executeQueue = ArrayDeque<() -> Unit>()
    private lateinit var console: ChannelLoggingHandler

    val client = createDiscordProvider().apply {
        logger.info("Using ${this.javaClass.simpleName} as jdaProvider")
        addListener(this@DiscordReceiver::attachConsole)
        addListener { it.on(MessageCreateEvent::class.java).subscribe(this@DiscordReceiver::dispatchMessage) }
        addListener {
            while (executeQueue.isNotEmpty()) {
                executeQueue.removeFirst()()
            }
        }
    }

    override fun onDisable() {
        client.shutdown()
        super.onDisable()
    }

    override fun handle(context: MessageContext): Long? {
        val sticker = config.messages.sticker.replace("<sticker_name>", (context.sticker ?: "null"))
        val attachment = config.messages.attachment
        val base = getMessageOrBase(context.platform)

        var text = (context.message ?: "")
        var images = ""
        for (img in context.images) {
            images += "\n" + img
        }
        text += images

        val preparedMessage = base.formatByMap(
            mapOf(
                "role" to (context.role ?: ""),
                "nickname" to context.senderName,
                "platform" to context.platform,
                "message" to text,
                "sticker" to (if (context.sticker != null) sticker else ""),
                "attachment" to (if (context.attachment) attachment else "")
            )
        )
        return sendMessageToChannel(
            if (config.phraseFilter.filterMessages) run {
                var msg = preparedMessage
                config.phraseFilter.filters.forEach { (k, v) -> msg = msg.replace(k, v) } //TODO make it optimized
                return@run msg
            } else preparedMessage
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
        var message: String
        var color: Color
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
            val textChannel = getChannelBlocking(channel) ?: return@addAction
            val headUrl = MultiMessageBridge.inst.headProvider.getHeadUrl(author)
            textChannel.createMessage(
                EmbedCreateSpec.create()
                    .withAuthor(Author.of(message, null, headUrl))
                    .withColor(discord4j.rest.util.Color.of(color.red, color.green, color.blue))
            ).subscribe()
        }

    fun sendMessageToChannel(message: String, channel: String = "main_text", replyId: Long? = null): Long? {
        val channel = findChannel(channel)?.id ?: return null
        val textChannel = getChannelBlocking(channel) ?: return null
        var spec = textChannel.createMessage(message).withContent(message)
        if (replyId != null) {
            val id = Snowflake.of(replyId)
            try {
                spec = spec.withMessageReferenceId(id)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        val message = spec.block()
        return message?.id?.asLong()
    }

    private fun getChannelBlocking(id: Long) = client.get().getChannelById(Snowflake.of(id))
        .ofType(GuildMessageChannel::class.java)
        .block()

    fun dispatchMessage(event: MessageCreateEvent) {
        val bukkitEvent = AsyncDiscordMessageEvent(event, this)
        if (!bukkitEvent.callEvent()) return
        if (event.message.author.getOrNull()?.isBot == true && bukkitEvent.mustBeCanceledIfBot) return
        //MultiMessageBridge.inst.logger.info(event.toString())
        val channelConfig = findChannel(id = event.message.channelId.asLong()) ?: return

        val stickerItems = event.message.stickersItems
        val sticker = if (stickerItems.isNotEmpty()) stickerItems.last().name else null

        val images = event.message.attachments.filter {
            it.contentType.isPresent && it.contentType.get().startsWith("image")
        }.map { it.url }

        var attachment = false
        for (atchmnt in event.message.attachments) {
            if (!(atchmnt.contentType.getOrNull()?.startsWith("image") ?: true)) {
                attachment = true
                break
            }
        }

        when (channelConfig.type) {
            "main_text" -> {
                val context = MessageContext(
                    senderName = event.member.getOrNull()?.displayName ?: event.message.author.getOrNull()?.username
                    ?: "unknown",
                    senderPlatformId = event.member.getOrNull()?.id?.asLong(),
                    message = event.message.content,
                    messagePlatformId = event.message.id.asLong(),
                    sticker = sticker,
                    images = images,
                    platform = name,
                    replyId = event.message.referencedMessage.getOrNull()?.id?.asLong(),
                    replyUser = event.message.referencedMessage.getOrNull()?.authorAsMember?.block()?.displayName
                        ?: event.message.referencedMessage.getOrNull()?.author?.getOrNull()?.username ?: "unknown",
                    role = event.member.getOrNull()?.highestRole?.map { it.name }
                        ?.onErrorResume { _ -> Mono.just("Player") }
                        ?.block(),
                    roleColor = event.member.getOrNull()?.highestRole?.map { r -> r.color }?.blockOptional()
                        ?.getOrNull()?.let { Color(it.red, it.green, it.blue) },
                    attachment = attachment,
                )
                dispatch(context)
            }

            "console" -> {
                //console.dumpStack()
                if (config.bot.commandsShouldStartsWithPrefix && !event.message.content.startsWith(config.bot.commandPrefix)) return
                dispatch(ConsoleMessageContext(event.message.content.substring(config.bot.commandPrefix.length)))
            }
        }
    }

    private fun getMessageOrBase(platform: String): String =
        config.messages.customFormats[platform]?.format
            ?: config.messages.messageBase.format

    private fun addAction(act: () -> Unit) {
        if (client.isInitialized()) {
            act()
        } else {
            executeQueue.add(act)
        }
    }

    private fun createDiscordProvider(): DiscordProvider {
        val token = config.bot.token
        val manager = Discord4jProviderManager().apply {
            add("default") { token, receiver -> CommonDiscordProvider(token, receiver) }
        }
        DiscordProviderRegistrationEvent(manager).callEvent()

        fun useCommon(error: String? = null): DiscordProvider {
            error?.let { MultiMessageBridge.inst.logger.warning("Using common provider as JDA provider | $it") }
            return CommonDiscordProvider(token, this)
        }

        if (manager.providers.isEmpty()) {
            return useCommon("No available JDA providers found!")
        }
        val providerName = config.advanced.jdaProvider

        return manager.providers[providerName]?.apply(token, this) ?: useCommon("Unknown provider $providerName")
    }

    private fun attachConsole(client: GatewayDiscordClient) {
        MultiMessageBridge.inst.logger.info("Attaching console appender")
        console =
            ChannelLoggingHandler(
                client.getChannelById(Snowflake.of(findChannel("console")?.id ?: 0))
                    .ofType(GuildMessageChannel::class.java)
            ) { config ->
                config.isColored = true
                config.mapLoggerName("net.minecraft.top.server.MinecraftServer", "Server")
                config.mapLoggerName("discord4j", "Discord")
                config.mapLoggerNameFriendly("net.minecraft.server") { s -> "Server/$s" }
                config.mapLoggerNameFriendly("net.minecraft") { s -> "Minecraft/$s" }
            }.attachLog4jLogging().schedule()
        MultiMessageBridge.inst.logger.info("ConsoleAppender must be attached")
    }

    fun findChannel(type: String? = null, id: Long? = null): ChannelConfiguration? =
        config.bot.channels.values.find { it.type == type || it.id == id }

    override fun handle(context: ServerInfoContext) = addAction {
        for ((_, j) in config.bot.channels) {
            client.get().getChannelById(Snowflake.of(j.id))
                .ofType(TextChannel::class.java)
                .flatMap { c ->
                    c.edit(
                        TextChannelEditSpec.create()
                            .withTopic(
                                ServerInfoProvider.parse(if (j.description == "") context.text else j.description) ?: ""
                            )
                    )
                }.subscribe()
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