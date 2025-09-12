package top.azimkin.multiMessageBridge.entities.repo

import top.azimkin.multiMessageBridge.entities.MessagePlatformMapping
import java.util.*

interface PlatformMappingRepo {
    fun createMapping(mapping: MessagePlatformMapping): Boolean
    fun delete(mapping: MessagePlatformMapping): Boolean
    fun deleteUpTo(timestamp: Date): Int
    fun findMessagePlatforms(messageId: Long): List<MessagePlatformMapping>
    fun findByPlatformMessageId(platform: String, messageId: Long): MessagePlatformMapping?
}