package top.azimkin.multiMessageBridge.platforms

import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InputMediaPhoto
import com.pengrad.telegrambot.request.GetFile
import com.pengrad.telegrambot.request.SendMediaGroup
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.MessagesResponse
import com.pengrad.telegrambot.response.SendResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import top.azimkin.multiMessageBridge.MessagingEventManager
import top.azimkin.multiMessageBridge.MultiMessageBridge
import top.azimkin.multiMessageBridge.api.events.AsyncTelegramOnUpdateEvent
import top.azimkin.multiMessageBridge.configuration.TelegramReceiverConfig
import top.azimkin.multiMessageBridge.data.*
import top.azimkin.multiMessageBridge.platforms.dispatchers.MessageDispatcher
import top.azimkin.multiMessageBridge.platforms.handlers.*
import top.azimkin.multiMessageBridge.utilities.formatByMap
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.regex.Pattern

class TelegramReceiver(val em: MessagingEventManager) :
    ConfigurableReceiver<TelegramReceiverConfig>("Telegram", TelegramReceiverConfig::class.java),
    MessageHandler, AdvancementHandler,
    MessageDispatcher, PlayerLifeHandler, SessionHandler, ServerSessionHandler {
    val token = config.bot.token
    var updateErrorsInPeriod: Int = 0
    val bot: TelegramBot

    init {
        if (TOKEN_PATTERN.matcher(token).matches()) bot = TelegramBot(token).also {
            it.setUpdatesListener({ updates ->
                for (update in updates) {
                    processUpdate(update)
                }
                updateErrorsInPeriod = 0
                return@setUpdatesListener UpdatesListener.CONFIRMED_UPDATES_ALL
            }) { _ ->
                updateErrorsInPeriod++
                if (updateErrorsInPeriod % 10 == 0)
                    MultiMessageBridge.inst.logger.warning("Unable to get updates from telegram API!\nErrors in period: $updateErrorsInPeriod")
            }
        } else {
            throw RuntimeException("Invalid telegram token provided!")
        }
    }

    override fun onDisable() {
        bot.shutdown()
        super.onDisable()
    }

    override fun handle(context: MessageContext): Long? {
        return sendMessage(parseFormat(context), replyToMessageId = context.replyId?.toInt(), images = context.images)
    }

    private fun parseFormat(context: MessageContext): String {
        val sticker = config.messages.sticker.replace("<sticker_name>", (context.sticker ?: "null"))
        val attachment = config.messages.attachment
        val base = (config.messages.customFormats[context.platform] ?: config.messages.messageBase).format

        return base.formatByMap(
            mapOf(
                "nickname" to context.senderName,
                "platform" to context.platform,
                "role" to (context.role ?: ""),
                "message" to (context.message ?: ""),
                "sticker" to (if (context.sticker != null) sticker else ""),
                "attachment" to (if (context.attachment) attachment else "")
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

    fun sendMessage(
        text: String,
        theme: String = "chat",
        times: Int = 0,
        replyToMessageId: Int? = null,
        images: List<String>? = null
    ): Long? {
        var answer: Long? = null
        val latch = CountDownLatch(1)

        if (!images.isNullOrEmpty()) {
            val mediaItems = images.map { url ->
                InputMediaPhoto(url).apply {
                    if (images.indexOf(url) == 0) {
                        showCaptionAboveMedia(true)
                        caption(text)
                    }
                }
            }

            bot.execute(
                SendMediaGroup(config.bot.mainChat, *mediaItems.toTypedArray()).apply {
                    if (config.bot.mainThread > -1) messageThreadId(config.bot.mainThread)
                    if (replyToMessageId != null) replyToMessageId(replyToMessageId)
                },
                object : Callback<SendMediaGroup, MessagesResponse> {
                    override fun onResponse(request: SendMediaGroup?, response: MessagesResponse?) {
                        answer = response?.messages()?.get(0)?.messageId()?.toLong()
                        latch.countDown()
                    }

                    override fun onFailure(request: SendMediaGroup?, e: IOException?) {
                        if (times < config.bot.sendMessageLimit) {
                            Thread.sleep(config.bot.timeUntilNextTry)
                            sendMessage(text, theme, times + 1, replyToMessageId, images)
                        } else {
                            MultiMessageBridge.inst.logger.warning("Unable to send media group to telegram, limit of tries has reached\nContent: $text\nImages: $images")
                            e?.printStackTrace()
                        }
                        latch.countDown()
                    }
                }
            )
        } else {
            bot.execute(
                SendMessage(config.bot.mainChat, text).apply {
                    if (config.bot.mainThread > -1) messageThreadId(config.bot.mainThread)
                    if (replyToMessageId != null) replyToMessageId(replyToMessageId)
                },
                object : Callback<SendMessage, SendResponse> {
                    override fun onResponse(request: SendMessage?, response: SendResponse?) {
                        answer = response?.message()?.messageId()?.toLong()
                        latch.countDown()
                    }

                    override fun onFailure(request: SendMessage?, e: IOException?) {
                        if (times < config.bot.sendMessageLimit) {
                            Thread.sleep(config.bot.timeUntilNextTry)
                            sendMessage(text, theme, times + 1, replyToMessageId, images)
                        } else {
                            MultiMessageBridge.inst.logger.warning("Unable to send message to telegram, limit of tries has reached\nContent: $text")
                            e?.printStackTrace()
                        }
                        latch.countDown()
                    }
                }
            )
        }

        latch.await()
        return answer
    }

    override fun handle(context: SessionContext) {
        val message = if (context.isJoined) {
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
                val imageBytes = getPhotoAsBase64(update)

                val images = imageBytes?.let { listOf(it) } ?: listOf()

                val attachments = update.message().video() != null
                        || update.message().audio() != null
                        || update.message().document() != null
                        || update.message().animation() != null  // gifs???
                var text = update.message().text()
                if (update.message().photo()?.isNotEmpty() ?: false)
                    text = update.message().caption()
                dispatch(
                    MessageContext(
                        senderName = update.message().from().username() ?: update.message().from().firstName()
                        ?: "Anonymous",
                        senderPlatformId = update.message().from().id(),
                        message = text,
                        messagePlatformId = update.message().messageId().toLong(),
                        images = images,
                        sticker = update.message().sticker()?.emoji(),
                        platform = name,
                        replyId = update.message()?.replyToMessage()?.messageId()?.toLong(),
                        replyUser = update.message()?.replyToMessage()?.from()?.username(),
                        attachment = attachments
                    )
                )
            }
        }
    }

    private fun getPhotoAsBase64(update: Update): String? {
        val photos = update.message().photo() ?: return null
        val photo = photos.last()

        try {
            val getFileRequest = GetFile(photo.fileId())
            val getFileResponse = bot.execute(getFileRequest)

            if (getFileResponse.isOk && getFileResponse.file() != null) {
                val file = getFileResponse.file()!!
                if (file.fileSize() >= 64 * 1024 * 1024) {
                    throw Exception("Image file is too big!")  // TODO handle
                }

                val fileUrl = bot.getFullFilePath(file)

                val request = Request.Builder().url(fileUrl).build()
                val httpClient = OkHttpClient()
                val response = httpClient.newCall(request).execute()

                if (response.isSuccessful && response.body != null) {
                    val imageBytes = response.body!!.bytes()
                    return Base64.getEncoder().encodeToString(imageBytes)
                }

                throw RuntimeException("Failed to download image from $fileUrl")

            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return null
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
        private val TOKEN_PATTERN = Pattern.compile("""^\d{9,10}:[A-Za-z0-9_-]{35}$""")
    }
}