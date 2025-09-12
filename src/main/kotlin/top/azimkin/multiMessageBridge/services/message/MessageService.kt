package top.azimkin.multiMessageBridge.services.message

import top.azimkin.multiMessageBridge.entities.CrossPlatformMessage
import top.azimkin.multiMessageBridge.entities.MessagePlatformMapping
import top.azimkin.multiMessageBridge.entities.repo.MessageRepo
import top.azimkin.multiMessageBridge.entities.repo.PlatformMappingRepo

interface MessageService {
    fun createNewMessage(
        authorUsername: String,
        text: String,
        sticker: String? = null,
        replyToId: Long? = null
    ): CrossPlatformMessage?

    fun addPlatformMapping(
        message: CrossPlatformMessage,
        platform: String,
        platformMessageId: Long?,
        platformMessageText: String
    ): Boolean

    fun findMessageByPlatformId(messageId: Long, platform: String): CrossPlatformMessage?

    fun getMessage(id: Long): CrossPlatformMessage?
}