package top.azimkin.multiMessageBridge.platforms

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.PhotoSize
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.GetFile
import com.pengrad.telegrambot.request.SendMessage
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.api.events.AsyncTelegramOnUpdateEvent
import top.azimkin.multiMessageBridge.data.MessageContext
import top.azimkin.multiMessageBridge.data.PlayerLifeContext
import top.azimkin.multiMessageBridge.data.ServerSessionContext
import top.azimkin.multiMessageBridge.data.SessionContext
import top.azimkin.multiMessageBridge.platforms.dispatchers.MessageDispatcher
import top.azimkin.multiMessageBridge.platforms.handlers.MessageHandler
import top.azimkin.multiMessageBridge.platforms.handlers.PlayerLifeHandler
import top.azimkin.multiMessageBridge.platforms.handlers.ServerSessionHandler
import top.azimkin.multiMessageBridge.platforms.handlers.SessionHandler
import top.azimkin.multiMessageBridge.utilities.format

object TelegramReceiver : BaseReceiver(
    "Telegram",
    mapOf(
        "chatId" to 0,
        "theme" to -1,
        "TOKEN" to "unknown",
        "messages.format._base" to "<platform> <role> <nickname> -> <message>",
        "messages.format.dead" to "<death_message>",
        "messages.format.join" to "<nickname> has joined the game!",
        "messages.format.leave" to "<nickname> has left the game!",
        "messages.format.firstJoin" to "<nickname> has joined first time!",
        "messages.format.server_enabled" to "Server enabled!",
        "messages.format.server_disabled" to "Server disabled!",
        "logPackets" to false
    )
), MessageHandler, MessageDispatcher, PlayerLifeHandler, SessionHandler, ServerSessionHandler {
    val TOKEN = config.getString("TOKEN")
    val bot = TelegramBot(TOKEN).also {
        it.setUpdatesListener { updates ->
            for (update in updates) {
                processUpdate(update)
            }
            return@setUpdatesListener UpdatesListener.CONFIRMED_UPDATES_ALL
        }
    }



    override fun handle(context: MessageContext) {
        sendMessage(parseFormat(context))
    }

    private fun parseFormat(context: MessageContext): String {
        val base = config.getString("messages.format.${context.platform}")
            ?: config.getString("messages.format._base")
            ?: "${context.platform} <nickname> -> <message>"

        return base.format(
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
        val format = config.getTranslation("messages.format.dead")
        sendMessage(
            format.format(
                mapOf(
                    "nickname" to context.playerName,
                    "death_message" to context.deathSource
                )
            )
        )
    }

    fun sendMessage(text: String) {
        bot.execute(
            SendMessage(config.getLong("chatId"), text).apply {
                if (config.getInt("theme") > -1) messageThreadId(config.getInt("theme"))
            })
    }

    override fun handle(context: SessionContext) {
        var message = if (context.isJoined) {
            if (context.isFirstJoined) {
                config.getTranslation("messages.format.firstJoin")
            } else {
                config.getTranslation("messages.format.join")
            }
        } else {
            config.getTranslation("messages.format.leave")
        }
        sendMessage(
            message.format(
                mapOf(
                    "nickname" to context.playerName,
                    "is_joined" to context.isJoined.toString(),
                    "is_first_join" to context.isFirstJoined.toString()
                )
            )
        )
    }

    fun getPhotoLink(update: Update): String? {
        if (update.message().photo() == null) return null
        val photos: List<PhotoSize> = update.message().photo().toList()
        val largestPhoto: PhotoSize = photos.last()
        val fileId: String = largestPhoto.fileId()
        val getFile = GetFile(fileId)
        val file = bot.execute(getFile).file()
        return "https://api.telegram.org/file/bot${TOKEN}/${file.filePath()}"
    }

    fun getVideoLink(update: Update): String? {
        if (update.message().video() == null) return null
        val video = update.message().video()
        if (video.fileSize() > MAX_FILE_SIZE) return null
        val getFile = GetFile(video.fileId())
        val file = bot.execute(getFile).file()
        return "https://api.telegram.org/file/bot${TOKEN}/${file.filePath()}"
    }

    private const val MAX_FILE_SIZE = 25_000_000

    override fun handle(context: ServerSessionContext) {
        if (context.isTurnedOn) {
            sendMessage(config.getTranslation("messages.format.server_enabled"))
        } else {
            sendMessage(config.getTranslation("messages.format.server_disabled"))
        }
    }

    private fun processUpdate(update: Update) {
        val event = AsyncTelegramOnUpdateEvent(update)
        if (!event.callEvent()) return

        if (config.getBoolean("logPackets")) {
            MultiMessageBridge.inst.logger.info(update.toString())
        }
        if (update.message() != null && update.message().messageThreadId() != null) {
            if (update.message().chat().id() == config.getLong("chatId") && update.message()
                    .messageThreadId() == config.getInt("theme")
            ) {
                val link = getPhotoLink(update) ?: getVideoLink(update)
                if (update.message().text() == null && link == null) return
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
}