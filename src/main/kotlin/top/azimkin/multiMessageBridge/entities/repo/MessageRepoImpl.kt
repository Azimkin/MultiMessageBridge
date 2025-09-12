package top.azimkin.multiMessageBridge.entities.repo

import com.j256.ormlite.dao.Dao
import top.azimkin.multiMessageBridge.entities.CrossPlatformMessage
import java.util.*

class MessageRepoImpl(
    private val dao: Dao<CrossPlatformMessage, Long>
) : MessageRepo {
    override fun get(id: Long): CrossPlatformMessage? {
        return try {
            dao.queryBuilder().where().eq("id", id).queryForFirst()
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    override fun create(message: CrossPlatformMessage): Boolean {
        return try {
            dao.create(message) == 1
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    override fun update(message: CrossPlatformMessage): Boolean {
        return try {
            dao.update(message) == 1
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    override fun delete(message: CrossPlatformMessage): Int {
        return try {
            dao.delete(message)
        } catch (e: Throwable) {
            e.printStackTrace()
            0
        }
    }

    override fun deleteUpTo(timestamp: Date): Boolean {
        return try {
            val deleteBuilder = dao.deleteBuilder()
            deleteBuilder.where().lt("timestamp", timestamp)
            deleteBuilder.delete() == 1
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    override fun getRecent(limit: Int): List<CrossPlatformMessage> {
        return try {
            dao.queryBuilder()
                .orderBy("timestamp", false)
                .limit(limit.toLong())
                .query()
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }
}