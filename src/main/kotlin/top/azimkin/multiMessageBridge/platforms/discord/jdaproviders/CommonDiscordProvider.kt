package top.azimkin.multiMessageBridge.platforms.discord.jdaproviders

import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import top.azimkin.multiMessageBridge.platforms.discord.DiscordReceiver

class CommonDiscordProvider(token: String, receiver: DiscordReceiver) : DiscordProvider {
    val initQueue = ArrayDeque<(GatewayDiscordClient) -> Unit>()
    var client: GatewayDiscordClient? = null

    init {
        client = DiscordClientBuilder.create(token)
            .build()
            .login()
            .block()
        initQueue.forEach { it(client!!) }
        initQueue.clear()
    }

    override fun get(): GatewayDiscordClient {
        return client!!
    }

    override fun isInitialized(): Boolean {
        return client != null;
    }

    override fun addListener(listener: (GatewayDiscordClient) -> Unit) {
        if (isInitialized()) {
            listener(client!!)
        } else {
            initQueue.add(listener)
        }
    }

    override fun shutdown() {
        client?.logout()?.block()
    }
}