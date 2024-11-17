package top.azimkin.multiMessageBridge.platforms.discord

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener

class DiscordEventListener(val receiver: DiscordReceiver) : EventListener {
    override fun onEvent(event: GenericEvent) {
        if (event !is MessageReceivedEvent) return
        receiver.dispatchMessage(event)
    }
}