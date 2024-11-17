package top.azimkin.multiMessageBridge.utilities

import java.awt.Color

fun parseColor(color: String?): Color {
    if (color == null) return Color.BLACK
    val args = color.split(":").map { it.toIntOrNull() ?: 0 }.toMutableList()
    if (args.size < 3) {
        for (i in args.size..3) {
            args.add(255)
        }
    }
    return Color(args[0], args[1], args[2])
}

fun Color.toHex(): String {
    return "#%02x%02x%02x".format(this.red, this.green, this.blue)
}
