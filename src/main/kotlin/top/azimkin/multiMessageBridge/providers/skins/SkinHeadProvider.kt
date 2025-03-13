package top.azimkin.multiMessageBridge.providers.skins

interface SkinHeadProvider {
    fun getHeadUrl(player: String): String?
}