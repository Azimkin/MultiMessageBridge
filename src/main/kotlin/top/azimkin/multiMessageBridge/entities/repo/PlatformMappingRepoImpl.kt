package top.azimkin.multiMessageBridge.entities.repo
import com.j256.ormlite.dao.Dao
import top.azimkin.multiMessageBridge.entities.MessagePlatformMapping
import java.sql.SQLException
import java.util.Date

class PlatformMappingRepoImpl(
    private val dao: Dao<MessagePlatformMapping, Long>
) : PlatformMappingRepo{
    override fun createMapping(mapping: MessagePlatformMapping): Boolean {
        return try {
            dao.create(mapping) == 1
        } catch (e: Throwable){e.printStackTrace()
            false
        }
    }

    override fun delete(mapping: MessagePlatformMapping): Boolean {
        return try{
            dao.delete(mapping) == 1
        } catch (e: Throwable){e.printStackTrace()
            false
        }
    }

    override fun deleteUpTo(timestamp: Date): Int {
        return try{
            val deleteBuilder = dao.deleteBuilder()
            deleteBuilder.where().lt("timestamp", timestamp)
            deleteBuilder.delete()
        } catch (e: Throwable){e.printStackTrace()
            0
        }
    }

    override fun findByPlatformMessageId(platform: String, messageId: Long): MessagePlatformMapping? {
        return try {
            dao.queryBuilder().where()
                .eq("platformMessageId", messageId)
                .and().eq("platform", platform).queryForFirst()
        } catch (e: Throwable){e.printStackTrace()
            null
        }
    }

    override fun findMessagePlatforms(messageId: Long): List<MessagePlatformMapping> {
        return try {
            dao.queryBuilder()
                .where().eq("message_id", messageId)
                .query() ?: return emptyList()
        } catch (e: Throwable){e.printStackTrace()
            emptyList()
        }
    }
}