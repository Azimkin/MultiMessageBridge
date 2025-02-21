package top.azimkin.multiMessageBridge.data

data class PlayerLifeContext(
    val playerName: String,
    val deathSource: String
) : BaseContext
