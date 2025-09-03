package top.azimkin.multiMessageBridge.services.database

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import org.bukkit.plugin.Plugin
import top.azimkin.multiMessageBridge.entities.CrossPlatformMessage
import top.azimkin.multiMessageBridge.entities.MessagePlatformMapping
import java.sql.SQLException

abstract class BaseDatabaseManager(
    protected val plugin: Plugin
) : DatabaseManager {

    protected lateinit var connectionSource: ConnectionSource
    protected val daos = mutableMapOf<Class<*>, Dao<*, *>>()

    override fun <T, ID> createDao(clazz: Class<T>): Dao<T, ID> {
        return try {
            @Suppress("UNCHECKED_CAST")
            val dao = DaoManager.createDao(connectionSource, clazz) as Dao<T, ID>
            daos[clazz] = dao
            dao
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to create DAO for ${clazz.simpleName}: ${e.message}")
            throw e
        }
    }

    override fun <T, ID> getDao(clazz: Class<T>): Dao<T, ID> {
        @Suppress("UNCHECKED_CAST")
        return daos[clazz] as? Dao<T, ID>
            ?: throw IllegalStateException("DAO for ${clazz.simpleName} not initialized")
    }

    override fun getMessageDao(): Dao<CrossPlatformMessage, Long> {
        return getDao(CrossPlatformMessage::class.java)
    }

    override fun getMappingDao(): Dao<MessagePlatformMapping, Long> {
        return getDao(MessagePlatformMapping::class.java)
    }

    protected fun createTables() {
        try {
            TableUtils.createTableIfNotExists(connectionSource, CrossPlatformMessage::class.java)
            TableUtils.createTableIfNotExists(connectionSource, MessagePlatformMapping::class.java)
            plugin.logger.info("Database tables created/verified successfully")
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to create database tables: ${e.message}")
            throw e
        }
    }

    protected fun initializeDaos() {
        try {
            val messageDao = DaoManager.createDao(connectionSource, CrossPlatformMessage::class.java)
            val mappingDao = DaoManager.createDao(connectionSource, MessagePlatformMapping::class.java)

            daos[CrossPlatformMessage::class.java] = messageDao
            daos[MessagePlatformMapping::class.java] = mappingDao

            plugin.logger.info("DAOs initialized successfully")
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to initialize DAOs: ${e.message}")
            throw e
        }
    }

    override fun disable() {
        if (::connectionSource.isInitialized) {
            connectionSource.close()
            plugin.logger.info("Database connection closed")
        }
    }
}