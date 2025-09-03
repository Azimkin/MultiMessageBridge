package top.azimkin.multiMessageBridge.entities
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.util.Date

@DatabaseTable(tableName = "message_platform_mappings")
data class MessagePlatformMapping(
    @DatabaseField(generatedId = true)
    val id: Long = 0,

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true)
    val message: CrossPlatformMessage = CrossPlatformMessage(),

    @DatabaseField(canBeNull = false, index = true)
    val platform: String = "",

    @DatabaseField(canBeNull = false, index = true)
    val platformMessageId: Long = 0,

    @DatabaseField(canBeNull = false)
    val timestamp: Date = Date()
    )
