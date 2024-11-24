package top.azimkin.multiMessageBridge

import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import top.azimkin.multiMessageBridge.api.events.AsyncHeadImageProviderRegistrationEvent
import top.azimkin.multiMessageBridge.api.events.ReceiverRegistrationEvent
import top.azimkin.multiMessageBridge.commands.MainCommand
import top.azimkin.multiMessageBridge.configuration.ConfigManager
import top.azimkin.multiMessageBridge.configuration.MMBConfiguration
import top.azimkin.multiMessageBridge.data.ServerInfoContext
import top.azimkin.multiMessageBridge.listeners.CommonListener
import top.azimkin.multiMessageBridge.metadata.LuckPermsMetadataProvider
import top.azimkin.multiMessageBridge.metadata.NoMetadataProvider
import top.azimkin.multiMessageBridge.metadata.PlayerMetadataProvider
import top.azimkin.multiMessageBridge.metadata.VaultMetadataProvider
import top.azimkin.multiMessageBridge.skins.HeadProviderManager
import top.azimkin.multiMessageBridge.skins.LinkHeadProvider
import top.azimkin.multiMessageBridge.skins.SkinHeadProvider
import top.azimkin.multiMessageBridge.utilities.DateFormatter
import top.azimkin.multiMessageBridge.utilities.runBukkitAsync
import java.io.File

class MultiMessageBridge : JavaPlugin() {
    companion object {
        lateinit var inst: MultiMessageBridge private set
    }

    init {
        inst = this
    }

    var enabledIn = System.currentTimeMillis()
        private set
    lateinit var headProvider: SkinHeadProvider
        private set
    lateinit var metadataProvider: PlayerMetadataProvider
        private set
    private val configManager =
        ConfigManager<MMBConfiguration>(MMBConfiguration::class.java, File(dataFolder, "config.yml"))
    lateinit var pluginConfig: MMBConfiguration
        private set
    val uptime: Long
        get() = System.currentTimeMillis() - enabledIn
    val dateFormatter = DateFormatter { pluginConfig.timeFormat }

    override fun onEnable() {
        dataFolder.mkdir()
        // lol why not
        getCommand("mmb")?.apply {
            setExecutor(MainCommand)
            tabCompleter = MainCommand
        }

        server.pluginManager.registerEvents(CommonListener, this)

        reload()

        ReceiverRegistrationEvent(MessagingEventManager.get()).callEvent()
        logger.info("RegisteredReceivers: ")
        for ((i, j) in MessagingEventManager.get().receivers.withIndex()) {
            logger.info("${i + 1}. ${j.name}")
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(
            this,
            this::updateServerInfo,
            pluginConfig.serverInfoUpdateTime.toLong()*20,
            pluginConfig.serverInfoUpdateTime.toLong()*20
        )
    }

    override fun onDisable() {
        MessagingEventManager.get().receivers.forEach { it.onDisable() }
    }

    fun reload() {
        reloadConfig()
        runBukkitAsync {
            val mgr = HeadProviderManager()
            mgr.add("default" to LinkHeadProvider::class.java)
            AsyncHeadImageProviderRegistrationEvent(mgr).callEvent()
            headProvider = mgr.createByName(pluginConfig.heads.provider, pluginConfig.heads.url)
        }
        setupMetadataProvider()
    }

    fun setEnabled() {
        enabledIn = System.currentTimeMillis()
    }

    fun setupMetadataProvider() {
        metadataProvider =
            if (server.pluginManager.getPlugin("Vault") != null && server.servicesManager.getRegistration(Chat::class.java) != null) {
                VaultMetadataProvider()
            } else if (server.pluginManager.getPlugin("LuckPerms") != null) {
                LuckPermsMetadataProvider()
            } else {
                NoMetadataProvider()
            }
    }

    fun updateServerInfo() {
        MessagingEventManager.get().dispatch(ServerInfoContext(pluginConfig.defaultServerInfoFormat))
    }

    override fun reloadConfig() {
        pluginConfig = configManager.loadOrUseDefault()
    }
}
