package top.azimkin.multiMessageBridge.providers.username

interface UsernameProvider {
    fun getUsername(id: Long, platform: String): String?
}