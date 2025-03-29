package top.azimkin.multiMessageBridge.api.events

import discord4j.core.event.domain.message.MessageCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import top.azimkin.multiMessageBridge.platforms.discord.Discord4jReceiver
import top.azimkin.multiMessageBridge.platforms.discord.DiscordReceiver

class AsyncDiscordMessageEvent(
    val event: MessageCreateEvent,
    val receiver: Discord4jReceiver,
    var mustBeCanceledIfBot: Boolean = true
) : Event(true),
    Cancellable {
    private var cancelled: Boolean = false
    override fun getHandlers(): HandlerList = handlerList

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}