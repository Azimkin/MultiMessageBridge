package top.azimkin.multiMessageBridge.data

data class ServerSessionContext(
    val isTurnedOn: Boolean,
    val isReload: Boolean = false,
) : BaseContext
