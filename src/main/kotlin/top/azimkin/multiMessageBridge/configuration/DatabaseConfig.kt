package top.azimkin.multiMessageBridge.configuration
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment

data class DatabaseConfig(
    @Comment("Should plugin use bStats and publish metrics on https://bstats.org/plugin/bukkit/MultiMessageBridge/24055")
    var type: String = "sqlite",
    @Comment("This part describes anything about heads?")
    var sqlite: SQLiteConfig = SQLiteConfig(),
    @Comment("Message receivers that must be enabled")
    var mysql: MySQLConfig = MySQLConfig(),

    var cleanup: CleanupConfig = CleanupConfig(),
) : OkaeriConfig()

data class CleanupConfig(
    @Comment("Cleanup interval in MINUTES")
    var intervalMinutes: Int = 24 * 60,
    @Comment("Cleans every message older that that many HOURS")
    var olderThanHours: Int = 24 * 7
) : OkaeriConfig()

data class SQLiteConfig(
    var name: String = "messages"
) : OkaeriConfig()

data class MySQLConfig(
    var host: String = "localhost",
    var port: Int = 3306,
    var database: String = "messages",
    var username: String = "root",
    var password: String = "",
    var useSSL: Boolean = false,
    var params: MutableMap<String, String> = mutableMapOf(
        "serverTimezone" to "UTC",
        "characterEncoding" to "UTF-8",
        "autoReconnect" to "true",
        "maxReconnects" to "10",
        "connectionTimeout" to "30000"
    )
) : OkaeriConfig()