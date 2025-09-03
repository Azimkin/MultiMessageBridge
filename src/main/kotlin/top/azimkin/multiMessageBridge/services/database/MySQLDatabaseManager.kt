package top.azimkin.multiMessageBridge.services.database

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.jdbc.db.MysqlDatabaseType
import org.bukkit.plugin.Plugin
import java.sql.SQLException

class MySQLDatabaseManager(
    plugin: Plugin,
    private val config: MySQLConfig
) : BaseDatabaseManager(plugin) {

    data class MySQLConfig(
        val host: String,
        val port: Int,
        val database: String,
        val username: String,
        val password: String,
        val useSSL: Boolean = false,
        val connectionParams: Map<String, String> = emptyMap()
    )

    override fun init() {
        try {
            val jdbcUrl = buildJdbcUrl()
            connectionSource = JdbcConnectionSource(jdbcUrl, MysqlDatabaseType())

            createTables()
            initializeDaos()

            plugin.logger.info("MySQL database initialized: ${config.host}:${config.port}/${config.database}")
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to initialize MySQL database: ${e.message}")
            throw e
        }
    }

    private fun buildJdbcUrl(): String {
        val params = mutableMapOf(
            "useSSL" to config.useSSL.toString(),
            "serverTimezone" to "UTC",
            "characterEncoding" to "UTF-8"
        )

        params.putAll(config.connectionParams)

        val paramsString = params.entries
            .joinToString("&") { "${it.key}=${it.value}" }

        return "jdbc:mysql://${config.host}:${config.port}/${config.database}?$paramsString"
    }
}