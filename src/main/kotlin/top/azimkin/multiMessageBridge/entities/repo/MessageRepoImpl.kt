package top.azimkin.multiMessageBridge.entities.repo
import com.j256.ormlite.dao.Dao
import top.azimkin.multiMessageBridge.entities.CrossPlatformMessage
import java.sql.SQLException
import java.util.Date

class MessageRepoImpl(private val dao: Dao<CrossPlatformMessage, Long>
) : MessageRepo{
    override fun get(id: Long): CrossPlatformMessage? {
        return try {
            dao.queryBuilder().where().eq("message_id", id).queryForFirst()
        } catch (_: SQLException) {
            null
        }
    }

    override fun create(message: CrossPlatformMessage): Boolean {
        return try {
            dao.create(message) == 1
        } catch (_: SQLException) {
            false
        }
    }

    override fun update(message: CrossPlatformMessage): Boolean {
        return try {
            dao.update(message) == 1
        } catch (_: SQLException) {
            false
        }
    }

    override fun delete(message: CrossPlatformMessage): Int {
        return try{
            dao.delete(message)
        } catch (_: SQLException){
            0
        }
    }

    override fun deleteUpTo(timestamp: Date): Boolean {
        return try{
            val deleteBuilder = dao.deleteBuilder()
            deleteBuilder.where().lt("timestamp", timestamp)
            deleteBuilder.delete() == 1
        } catch (_: SQLException){
            false
        }
    }

    override fun getRecent(limit: Int): List<CrossPlatformMessage> {
        return try {
            dao.queryBuilder()
                .orderBy("timestamp", false)
                .limit(limit.toLong())
                .query()
        } catch (_: SQLException) {
            emptyList()
        }
    }
}