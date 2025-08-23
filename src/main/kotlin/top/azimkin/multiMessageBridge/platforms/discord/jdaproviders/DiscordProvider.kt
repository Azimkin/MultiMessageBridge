package top.azimkin.multiMessageBridge.platforms.discord.jdaproviders

import discord4j.core.GatewayDiscordClient
import java.util.function.Consumer

interface DiscordProvider {
    fun get(): GatewayDiscordClient

    fun isInitialized(): Boolean

    fun addListener(listener: Consumer<GatewayDiscordClient>)

    fun shutdown()
}