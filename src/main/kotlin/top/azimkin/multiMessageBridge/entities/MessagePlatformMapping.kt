package top.azimkin.multiMessageBridge.entities
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.util.Date

@DatabaseTable(tableName = "message_platform_mappings")
data class MessagePlatformMapping(
    @DatabaseField(generatedId = true)
    var id: Long = 0,

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true)
    var message: CrossPlatformMessage = CrossPlatformMessage(),

    @DatabaseField(canBeNull = false, index = true)
    var platform: String = "",

    @DatabaseField(index = true)
    var platformMessageId: Long? = null,

    @DatabaseField(canBeNull = false)
    var platformMessageText: String = "",

    @DatabaseField(canBeNull = false)
    var timestamp: Date = Date()
    )
