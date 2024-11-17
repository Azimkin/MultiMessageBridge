package top.azimkin.multiMessageBridge.platforms.discord.jdaproviders

import net.dv8tion.jda.api.JDA

interface JdaProvider {
    fun get(): JDA

    fun isInitialized(): Boolean

    fun addInitializeListener(listener: (JDA) -> Unit)
}