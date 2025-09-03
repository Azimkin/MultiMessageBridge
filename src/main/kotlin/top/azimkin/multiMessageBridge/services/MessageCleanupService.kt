package top.azimkin.multiMessageBridge.services

import org.bukkit.plugin.Plugin
import top.azimkin.multiMessageBridge.configuration.DatabaseConfig
import top.azimkin.multiMessageBridge.entities.repo.MessageRepo
import top.azimkin.multiMessageBridge.entities.repo.PlatformMappingRepo
import java.util.concurrent.TimeUnit
import java.util.Date

class MessageCleanupService(
    private val plugin: Plugin,
    private val messageRepo: MessageRepo,
    private val mappingRepo: PlatformMappingRepo,
    private val config: DatabaseConfig
) {
    fun cleanupOldMessages() {
        if (config.cleanup.intervalMinutes <= 0)
            return

        val cutoffDate = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(config.cleanup.olderThanHours.toLong()))
        
        messageRepo.deleteUpTo(cutoffDate)
        mappingRepo.deleteUpTo(cutoffDate)
    }

    fun scheduleCleanupTask() {
        if (config.cleanup.intervalMinutes <= 0) 
            return

        val intervalTicks = config.cleanup.intervalMinutes * 60 * 20L

        plugin.server.scheduler.runTaskTimerAsynchronously(
            plugin,
            Runnable {
                cleanupOldMessages()
            },
            intervalTicks,
            intervalTicks
        )
    }
}