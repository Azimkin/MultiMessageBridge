package top.azimkin.multiMessageBridge.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import top.azimkin.multiMessageBridge.platforms.discord.jdaproviders.Discord4jProviderManager

class DiscordProviderRegistrationEvent(
    val jdaProviderManager: Discord4jProviderManager,
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}