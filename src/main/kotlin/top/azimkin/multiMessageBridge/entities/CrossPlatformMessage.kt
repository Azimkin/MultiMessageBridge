package top.azimkin.multiMessageBridge.entities
import com.j256.ormlite.dao.ForeignCollection
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.ForeignCollectionField
import com.j256.ormlite.table.DatabaseTable
import com.pengrad.telegrambot.model.Message
import java.util.Date

@DatabaseTable(tableName = "messages")
data class CrossPlatformMessage(
    @DatabaseField(generatedId = true)
    var id: Long = 0,

    @DatabaseField(canBeNull = false, index = true)
    var authorUsername: String = "",

    @DatabaseField(canBeNull = false, width = 5000)
    var text: String = "",

    @DatabaseField
    var sticker: String? = null,

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    var replyTo: CrossPlatformMessage? = null,

    @ForeignCollectionField
    var mappings: ForeignCollection<MessagePlatformMapping>? = null,

    @DatabaseField(canBeNull = false)
    var timestamp: Date = Date(),
)
