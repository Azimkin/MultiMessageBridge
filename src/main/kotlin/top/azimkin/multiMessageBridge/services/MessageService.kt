package top.azimkin.multiMessageBridge.services

import top.azimkin.multiMessageBridge.entities.CrossPlatformMessage
import top.azimkin.multiMessageBridge.entities.MessagePlatformMapping
import top.azimkin.multiMessageBridge.entities.repo.MessageRepo
import top.azimkin.multiMessageBridge.entities.repo.PlatformMappingRepo

class MessageService(
    private val messageRepo: MessageRepo,
    private val mappingRepo: PlatformMappingRepo
) {

    fun createNewMessage(
        authorUsername: String,
        text: String,
        sticker: String? = null,
        replyToId: Long? = null
    ): CrossPlatformMessage? {

        val replyTo = replyToId?.let { messageRepo.get(it) }

        val message = CrossPlatformMessage(
            authorUsername = authorUsername,
            text = text,
            sticker = sticker,
            replyTo = replyTo
        )

        return if (messageRepo.create(message)) message else null
    }

    fun addPlatformMapping(
        message: CrossPlatformMessage,
        platform: String,
        platformMessageId: Long?,
        platformMessageText: String
    ): Boolean {
        return mappingRepo.createMapping(
            MessagePlatformMapping(
                message = message,
                platform = platform,
                platformMessageId = platformMessageId,
                platformMessageText = platformMessageText
            )
        )
    }

    fun findMessageByPlatformId(messageId: Long, platform: String) =
        mappingRepo.findByPlatformMessageId(platform, messageId)?.message


    fun getMessage(id: Long) = messageRepo.get(id)
}