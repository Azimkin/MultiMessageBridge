package top.azimkin.multiMessageBridge.platforms.discord

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.lifecycle.ReadyEvent
import top.azimkin.multiMessageBridge.configuration.DiscordReceiverConfig
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.data.PlayerLifeContext
import top.azimkin.multiMessageBridge.data.ServerSessionContext
import top.azimkin.multiMessageBridge.data.SessionContext
import top.azimkin.multiMessageBridge.platforms.ConfigurableReceiver
import top.azimkin.multiMessageBridge.platforms.dispatchers.MessageDispatcher
import top.azimkin.multiMessageBridge.platforms.handlers.MessageHandler
import top.azimkin.multiMessageBridge.platforms.handlers.PlayerLifeHandler
import top.azimkin.multiMessageBridge.platforms.handlers.ServerSessionHandler
import top.azimkin.multiMessageBridge.platforms.handlers.SessionHandler

class Discord4jReceiver() : ConfigurableReceiver<DiscordReceiverConfig>("Discord4j", DiscordReceiverConfig::class.java),
    MessageHandler, MessageDispatcher, PlayerLifeHandler, SessionHandler, ServerSessionHandler {

    private lateinit var client: GatewayDiscordClient

    init {
        reload()
    }

    override fun reload() {
        super.reload()
        client = DiscordClient.create(config.bot.token)
            .login()
            .block()!!
        client.on(ReadyEvent::class.java).subscribe { event ->
            logger.info("Client ready!")
        }
    }

    override fun onDisable() {
        client.logout().block()
        super.onDisable()
    }

    override fun handle(context: MessageContext) {
        TODO("Not yet implemented")
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
}