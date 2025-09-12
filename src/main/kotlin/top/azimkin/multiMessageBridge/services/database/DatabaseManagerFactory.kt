package top.azimkin.multiMessageBridge.services.database

import org.bukkit.plugin.Plugin
import top.azimkin.multiMessageBridge.configuration.DatabaseConfig

class DatabaseManagerFactory {

    companion object {
        fun create(plugin: Plugin, databaseConfig: DatabaseConfig): DatabaseManager {
            return when (val databaseType = databaseConfig.type.lowercase()) {
                "mysql" -> createMySQLManager(plugin, databaseConfig)
                "sqlite" -> createSQLiteManager(plugin, databaseConfig)
                else -> throw IllegalArgumentException("Unsupported database type: $databaseType")
            }
        }

        private fun createSQLiteManager(plugin: Plugin, databaseConfig: DatabaseConfig): SQLiteDatabaseManager {
            val databaseName = databaseConfig.sqlite.name
            return SQLiteDatabaseManager(plugin, databaseName)
        }

        private fun createMySQLManager(plugin: Plugin, databaseConfig: DatabaseConfig): MySQLDatabaseManager {
            val mysqlConfig = MySQLDatabaseManager.MySQLConfig(
                host = databaseConfig.mysql.host,
                port = databaseConfig.mysql.port,
                database = databaseConfig.mysql.database,
                username = databaseConfig.mysql.username,
                password = databaseConfig.mysql.password,
                useSSL = databaseConfig.mysql.useSSL,
                connectionParams = databaseConfig.mysql.params
                    .mapValues { it.value }
            )

            return MySQLDatabaseManager(plugin, mysqlConfig)
        }
    }
}