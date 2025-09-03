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
        imageUrl: String? = null,
        sticker: String? = null,
        replyToId: Long? = null
    ): CrossPlatformMessage? {

        val replyTo = replyToId?.let { messageRepo.get(it) }

        val message = CrossPlatformMessage(
            authorUsername = authorUsername,
            text = text,
            imageUrl = imageUrl,
            sticker = sticker,
            replyTo = replyTo
        )

        return if (messageRepo.create(message)) message else null
    }

    fun addPlatformMapping(
        message: CrossPlatformMessage,
        platform: String,
        platformMessageId: Long
    ): Boolean {
        return mappingRepo.createMapping(
            MessagePlatformMapping(
                message = message,
                platform = platform,
                platformMessageId = platformMessageId
            )
        )
    }

    fun findMessageByPlatformId(platform: String, messageId: Long): CrossPlatformMessage? {
        return mappingRepo.findByPlatformMessageId(platform, messageId)?.message
    }

    fun getMessage(id: Long): Pair<CrossPlatformMessage?, List<MessagePlatformMapping>> {
        val message = messageRepo.get(id)
        val mappings = message?.let { mappingRepo.findMessagePlatforms(id) } ?: emptyList()
        return Pair(message, mappings)
    }
}