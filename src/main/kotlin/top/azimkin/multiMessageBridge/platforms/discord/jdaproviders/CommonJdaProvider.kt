package top.azimkin.multiMessageBridge.platforms.discord.jdaproviders

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.requests.GatewayIntent
import top.azimkin.multiMessageBridge.platforms.discord.DiscordEventListener
import top.azimkin.multiMessageBridge.platforms.discord.DiscordReceiver

class CommonJdaProvider(token: String, receiver: DiscordReceiver) : JdaProvider, EventListener {
    private val initializeHandlers = ArrayDeque<(JDA) -> Unit>()
    private val jda = JDABuilder
        .createLight(token)
        .addEventListeners(DiscordEventListener(receiver), this)
        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
        .build()
        .apply {
            awaitReady()
            while (initializeHandlers.isNotEmpty()) {
                initializeHandlers.removeFirst()(this)
            }
        }

    override fun get(): JDA = jda

    override fun isInitialized(): Boolean = jda.status == JDA.Status.CONNECTED

    override fun addInitializeListener(listener: (JDA) -> Unit) {
        initializeHandlers.add(listener)
    }

    override fun shutdown() {
        jda.shutdown()
        jda.awaitShutdown()
    }

    override fun onEvent(event: GenericEvent) {
        if (event is ReadyEvent) {
            while (initializeHandlers.isNotEmpty()) {
                initializeHandlers.removeFirst()(event.jda)
            }
        }
    }

}