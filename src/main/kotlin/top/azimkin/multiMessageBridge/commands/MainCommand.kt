package top.azimkin.multiMessageBridge.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import top.azimkin.multiMessageBridge.MultiMessageBridge
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

object MainCommand : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        if (args.isEmpty()) return false
        if (args[0] == "reload") {
            var isSuccess = false
            CompletableFuture.runAsync {
                try {
                    MultiMessageBridge.inst.reload()
                    MultiMessageBridge.inst.messagingEventManager.reloadAll()
                    isSuccess = true
                } catch (e: Exception) {
                    isSuccess = false
                    MultiMessageBridge.inst.logger.log(Level.SEVERE, e.message, e)
                }
            }.thenAccept {
                if (isSuccess) {
                    sender.sendMessage("Успешная перезагрузка плагина")
                } else {
                    sender.sendMessage("При выполнении перезагрузки плагина произошла непредвиденная ошибка!")
                }
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String?>? {
        if (sender.hasPermission("bridge.admin")) {
            return when (args.size) {
                1 -> listOf("reload")

                else -> emptyList()
            }
        }
        return emptyList()
    }
}