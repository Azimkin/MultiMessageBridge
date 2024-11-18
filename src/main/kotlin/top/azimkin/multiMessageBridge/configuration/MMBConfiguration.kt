package top.azimkin.multiMessageBridge.configuration

data class HeadsConfiguration(
    val url: String = "https://crafthead.net/helm/%nickname%",
    val provider: String = "default",
)

data class MMBConfiguration(
    val heads: HeadsConfiguration = HeadsConfiguration(),
)
