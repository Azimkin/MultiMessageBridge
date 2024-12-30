package top.azimkin.multiMessageBridge.platforms

import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.PhotoSize
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.GetFile
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.api.events.AsyncTelegramOnUpdateEvent
import top.azimkin.multiMessageBridge.configuration.TelegramReceiverConfig
import top.azimkin.multiMessageBridge.data.AdvancementContext
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.data.PlayerLifeContext
import top.azimkin.multiMessageBridge.data.ServerSessionContext
import top.azimkin.multiMessageBridge.data.SessionContext
import top.azimkin.multiMessageBridge.platforms.dispatchers.MessageDispatcher
import top.azimkin.multiMessageBridge.platforms.handlers.AdvancementHandler
import top.azimkin.multiMessageBridge.platforms.handlers.MessageHandler
import top.azimkin.multiMessageBridge.platforms.handlers.PlayerLifeHandler
import top.azimkin.multiMessageBridge.platforms.handlers.ServerSessionHandler
import top.azimkin.multiMessageBridge.platforms.handlers.SessionHandler
import top.azimkin.multiMessageBridge.utilities.formatByMap
import java.io.IOException

class TelegramReceiver : ConfigurableReceiver<TelegramReceiverConfig>("Telegram", TelegramReceiverConfig::class.java),
    MessageHandler, AdvancementHandler,
    MessageDispatcher, PlayerLifeHandler, SessionHandler, ServerSessionHandler {
    val token = config.bot.token
    var updateErrorsInPeriod: Int = 0
    val bot = TelegramBot(token).also {
        it.setUpdatesListener({ updates ->
            for (update in updates) {
                processUpdate(update)
            }
            updateErrorsInPeriod = 0
            return@setUpdatesListener UpdatesListener.CONFIRMED_UPDATES_ALL
        }) { e ->
            updateErrorsInPeriod++
            if (updateErrorsInPeriod % 10 == 0)
                MultiMessageBridge.inst.logger.warning("Unable to get updates from telegram API!\nErrors in period: $updateErrorsInPeriod")
        }
    }


    override fun handle(context: MessageContext) {
        sendMessage(parseFormat(context))
    }

    private fun parseFormat(context: MessageContext): String {
        val base = (config.messages.customFormats[context.platform] ?: config.messages.messageBase).format

        return base.formatByMap(
            mapOf(
                "nickname" to context.senderName,
                "platform" to context.platform,
                "role" to (context.role ?: ""),
                "message" to context.message,
                "reply_message" to (context.reply ?: ""),
                "reply_user" to (context.replyUser ?: ""),
            )
        )
    }

    override fun handle(context: PlayerLifeContext) {
        val format = config.messages.death.format
        sendMessage(
            format.formatByMap(
                mapOf(
                    "nickname" to context.playerName,
                    "death_message" to context.deathSource
                )
            )
        )
    }

    fun sendMessage(text: String, theme: String = "chat", times: Int = 0) {
        bot.execute(
            SendMessage(config.bot.mainChat, text).apply {
                if (config.bot.mainThread > -1) messageThreadId(config.bot.mainThread)
            },
            object : Callback<SendMessage, SendResponse> {
                override fun onResponse(
                    request: SendMessage?,
                    response: SendResponse?
                ) {
                }

                override fun onFailure(request: SendMessage?, e: IOException?) {
                    if (times < config.bot.sendMessageLimit) {
                        Thread.sleep(config.bot.timeUntilNextTry)
                        sendMessage(text, theme, times + 1)
                    } else {
                        MultiMessageBridge.inst.logger.warning("Unable to send message to telegram, limit of tries has reached\nContent: $text")
                        e?.printStackTrace()
                    }
                }
            }
        )

    }

    override fun handle(context: SessionContext) {
        var message = if (context.isJoined) {
            if (context.isFirstJoined) {
                config.messages.firstJoin
            } else {
                config.messages.join
            }
        } else {
            config.messages.leave
        }.format
        sendMessage(
            message.formatByMap(
                mapOf(
                    "nickname" to context.playerName,
                    "is_joined" to context.isJoined.toString(),
                    "is_first_join" to context.isFirstJoined.toString()
                )
            )
        )
    }

    // TODO
    fun getPhotoLink(update: Update): String? {
        if (update.message().photo() == null) return null
        val photos: List<PhotoSize> = update.message().photo().toList()
        val largestPhoto: PhotoSize = photos.last()
        val fileId: String = largestPhoto.fileId()
        val getFile = GetFile(fileId)
        val file = bot.execute(getFile).file()
        return "https://api.telegram.org/file/bot${token}/${file.filePath()}"
    }

    // TODO
    fun getVideoLink(update: Update): String? {
        if (update.message().video() == null) return null
        val video = update.message().video()
        if (video.fileSize() > MAX_FILE_SIZE) return null
        val getFile = GetFile(video.fileId())
        val file = bot.execute(getFile).file()
        return "https://api.telegram.org/file/bot${token}/${file.filePath()}"
    }


    override fun handle(context: ServerSessionContext) {
        if (context.isTurnedOn) {
            sendMessage(config.messages.serverEnabled.format)
        } else {
            sendMessage(config.messages.serverDisabled.format)
        }
    }

    private fun processUpdate(update: Update) {
        val event = AsyncTelegramOnUpdateEvent(update, bot)
        if (!event.callEvent()) return

        if (config.debug.logPackets) {
            MultiMessageBridge.inst.logger.info(update.toString())
        }
        if (config.debug.preConfiguredDebug) {
            MultiMessageBridge.inst.logger.info("Preconfigure debug enabled! Please, disable it in Telegram.yml after setting it up!")
            MultiMessageBridge.inst.logger.info(
                "    ChatId: ${
                    update.message()?.chat()?.id()
                } ThreadId: ${update.message()?.messageThreadId()} Message: ${update.message()?.text()}"
            )
        }
        if (update.message()?.chat()?.id() == config.bot.mainChat) {
            if (config.bot.mainThread < 0 || (update.message()?.messageThreadId() == config.bot.mainThread)) {
                val link = getPhotoLink(update) ?: getVideoLink(update)
                if (update.message()?.text() == null && link == null) return
                dispatch(
                    MessageContext(
                        update.message().from().username(),
                        update.message().text() ?: "",
                        update.message().replyToMessage() != null,
                        name,
                        update.message()?.replyToMessage()?.text(),
                        update.message()?.replyToMessage()?.from()?.username(),
                        null
                    )
                )
            }
        }
    }

    override fun handle(context: AdvancementContext) {
        sendMessage(
            config.messages.advancementGrant.format.formatByMap(
                mapOf(
                    "nickname" to context.playerName,
                    "advancement" to context.advancementName,
                    "description" to context.description,
                    "rarity" to context.rarity.name,
                )
            )
        )
    }

    companion object {
        private const val MAX_FILE_SIZE = 25_000_000
    }
}