package top.azimkin.multiMessageBridge.data

data class SessionContext(
    val playerName: String,
    val isJoined: Boolean,
    val isFirstJoined: Boolean = false,
)
