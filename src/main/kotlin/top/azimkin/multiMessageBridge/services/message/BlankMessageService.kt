package top.azimkin.multiMessageBridge.services.message

import top.azimkin.multiMessageBridge.entities.CrossPlatformMessage

class BlankMessageService : MessageService {
    override fun createNewMessage(
        authorUsername: String,
        text: String,
        sticker: String?,
        replyToId: Long?
    ): CrossPlatformMessage? {
        return CrossPlatformMessage(
            authorUsername = authorUsername,
            text = text,
            sticker = sticker,
        );

    }

    override fun addPlatformMapping(
        message: CrossPlatformMessage,
        platform: String,
        platformMessageId: Long?,
        platformMessageText: String
    ): Boolean {
        return true;
    }

    override fun findMessageByPlatformId(
        messageId: Long,
        platform: String
    ): CrossPlatformMessage? {
        return null;
    }

    override fun getMessage(id: Long): CrossPlatformMessage? {
        return null;
    }
}