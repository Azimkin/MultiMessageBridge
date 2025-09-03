package top.azimkin.multiMessageBridge.entities.repo
import top.azimkin.multiMessageBridge.entities.CrossPlatformMessage
import java.util.Date

interface MessageRepo{
    fun get(id: Long): CrossPlatformMessage?
    fun create(message: CrossPlatformMessage): Boolean
    fun update(message: CrossPlatformMessage): Boolean
    fun delete(message: CrossPlatformMessage): Int
    fun deleteUpTo(timestamp: Date): Boolean
    fun getRecent(limit: Int): List<CrossPlatformMessage>
}