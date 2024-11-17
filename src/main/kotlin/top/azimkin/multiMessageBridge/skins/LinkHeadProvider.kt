package top.azimkin.multiMessageBridge.skins

class LinkHeadProvider(val link: String) : SkinHeadProvider {
    override fun getHeadUrl(player: String): String? {
        return link.replace("%nickname%", player)
    }
}