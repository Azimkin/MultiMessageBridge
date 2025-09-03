package top.azimkin.multiMessageBridge.entities
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.util.Date

@DatabaseTable(tableName = "messages")
data class CrossPlatformMessage(
    @DatabaseField(generatedId = true)
    val id: Long = 0,

    @DatabaseField(canBeNull = false, index = true)
    val authorUsername: String = "",

    @DatabaseField(canBeNull = false, width = 5000)
    val text: String = "",

    @DatabaseField
    val imageUrl: String? = null,

    @DatabaseField
    val sticker: String? = null,

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    var replyTo: CrossPlatformMessage? = null,

    @DatabaseField(canBeNull = false)
    val timestamp: Date = Date(),
)
