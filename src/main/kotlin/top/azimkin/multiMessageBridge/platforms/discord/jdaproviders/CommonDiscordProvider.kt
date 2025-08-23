package top.azimkin.multiMessageBridge.platforms.discord.jdaproviders

import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import top.azimkin.multiMessageBridge.platforms.discord.DiscordReceiver
import java.util.function.Consumer

class CommonDiscordProvider(token: String, receiver: DiscordReceiver) : DiscordProvider {
    val initQueue = ArrayDeque<Consumer<GatewayDiscordClient>>()
    var client: GatewayDiscordClient? = null

    init {
        client = DiscordClientBuilder.create(token)
            .build()
            .login()
            .block()
        initQueue.forEach { it.accept(client!!) }
        initQueue.clear()
    }

    override fun get(): GatewayDiscordClient {
        return client!!
    }

    override fun isInitialized(): Boolean {
        return client != null;
    }

    override fun addListener(listener: Consumer<GatewayDiscordClient>) {
        if (isInitialized()) {
            listener.accept(client!!)
        } else {
            initQueue.add(listener)
        }
    }

    override fun shutdown() {
        client?.logout()?.block()
    }
}