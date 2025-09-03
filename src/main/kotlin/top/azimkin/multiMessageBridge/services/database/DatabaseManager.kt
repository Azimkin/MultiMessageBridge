package top.azimkin.multiMessageBridge.services.database

import com.j256.ormlite.dao.Dao
import top.azimkin.multiMessageBridge.entities.CrossPlatformMessage
import top.azimkin.multiMessageBridge.entities.MessagePlatformMapping
import java.sql.SQLException

interface DatabaseManager {
    fun init()

    fun getMessageDao(): Dao<CrossPlatformMessage, Long>

    fun getMappingDao(): Dao<MessagePlatformMapping, Long>

    @Throws(SQLException::class)
    fun <T, ID> getDao(clazz: Class<T>): Dao<T, ID>

    fun <T, ID> createDao(clazz: Class<T>): Dao<T, ID>

    fun disable()
}