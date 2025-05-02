package top.azimkin.multiMessageBridge.platforms.discord.jdaproviders

import discord4j.core.GatewayDiscordClient

interface DiscordProvider {
    fun get(): GatewayDiscordClient

    fun isInitialized(): Boolean

    fun addListener(listener: (GatewayDiscordClient) -> Unit)

    fun shutdown()
}