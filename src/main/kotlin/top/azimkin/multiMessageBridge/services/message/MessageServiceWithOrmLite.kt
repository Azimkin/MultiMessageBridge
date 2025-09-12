package top.azimkin.multiMessageBridge.services.message

import top.azimkin.multiMessageBridge.entities.CrossPlatformMessage
import top.azimkin.multiMessageBridge.entities.MessagePlatformMapping
import top.azimkin.multiMessageBridge.entities.repo.MessageRepo
import top.azimkin.multiMessageBridge.entities.repo.PlatformMappingRepo

class MessageServiceWithOrmLite(
    private val messageRepo: MessageRepo,
    private val mappingRepo: PlatformMappingRepo
) : MessageService {
    override fun createNewMessage(
        authorUsername: String,
        text: String,
        sticker: String?,
        replyToId: Long?
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

    override fun addPlatformMapping(
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

    override fun findMessageByPlatformId(messageId: Long, platform: String) =
        mappingRepo.findByPlatformMessageId(platform, messageId)?.message


    override fun getMessage(id: Long) = messageRepo.get(id)
}