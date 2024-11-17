package top.azimkin.multiMessageBridge.utilities

import org.bukkit.Bukkit
import top.azimkin.multiMessageBridge.MultiMessageBridge

fun runSync(runnable: () -> Unit) = Bukkit.getScheduler().runTask(MultiMessageBridge.inst, runnable)

fun runBukkitAsync(runnable: () -> Unit) =
    Bukkit.getScheduler().runTaskAsynchronously(MultiMessageBridge.inst, runnable)