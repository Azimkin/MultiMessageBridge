package top.azimkin.multiMessageBridge.utilities

import java.time.Duration

class DateFormatter(val format: () -> String) {
    fun format(timeInMillis: Long): String {
        println(timeInMillis)
        var format = format()
        var duration = Duration.ofMillis(timeInMillis)
        val days = format.contains("{d}")
        val hours = format.contains("{h}")
        val minutes = format.contains("{m}")

        if (days) {
            format = format.replace("{d}", duration.toDays().toString())
            duration = duration.minus(Duration.ofDays(duration.toDays()))
        }
        if (hours) {
            format = format.replace("{h}", duration.toHours().toString())
            duration = duration.minus(Duration.ofHours(duration.toHours()))
        }
        if (minutes) {
            format = format.replace("{m}", duration.toMinutes().toString())
        }

        return format
    }
}