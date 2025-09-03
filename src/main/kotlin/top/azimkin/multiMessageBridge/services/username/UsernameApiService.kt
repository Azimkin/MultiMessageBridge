package top.azimkin.multiMessageBridge.services.username

interface UsernameApiService {
    fun getUsername(id: Long, platform: String): String?
}