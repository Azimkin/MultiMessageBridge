package top.azimkin.multiMessageBridge.services.database

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.jdbc.db.SqliteDatabaseType
import org.bukkit.plugin.Plugin
import java.io.File
import java.sql.SQLException

class SQLiteDatabaseManager(
    plugin: Plugin,
    private val databaseName: String = "messages"
) : BaseDatabaseManager(plugin) {

    override fun init() {
        try {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }

            val databaseUrl = "jdbc:sqlite:${plugin.dataFolder.path}${File.separator}$databaseName.db"
            connectionSource = JdbcConnectionSource(databaseUrl, SqliteDatabaseType())

            createTables()
            initializeDaos()

            plugin.logger.info("SQLite database initialized: $databaseUrl")
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to initialize SQLite database: ${e.message}")
            throw e
        }
    }
}