package top.azimkin.multiMessageBridge

import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import top.azimkin.mmb.Metrics
import top.azimkin.multiMessageBridge.api.events.AsyncHeadImageProviderRegistrationEvent
import top.azimkin.multiMessageBridge.api.events.ReceiverRegistrationEvent
import top.azimkin.multiMessageBridge.commands.MainCommand
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
import top.azimkin.multiMessageBridge.utilities.Translator
import top.azimkin.multiMessageBridge.utilities.runBukkitAsync
import java.io.File

class MultiMessageBridge : JavaPlugin() {
    companion object {
        lateinit var inst: MultiMessageBridge private set
    }

    init {
        inst = this
    }

    var messagingEventManager: MessagingEventManager = MessagingEventManagerImpl(); private set
    var enabledIn = System.currentTimeMillis(); private set
    lateinit var headProvider: SkinHeadProvider private set
    lateinit var metadataProvider: PlayerMetadataProvider private set
    lateinit var pluginConfig: MMBConfiguration private set
    val uptime: Long get() = System.currentTimeMillis() - enabledIn
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

        ReceiverRegistrationEvent(messagingEventManager).callEvent()
        logger.info("RegisteredReceivers: ")
        for ((i, j) in messagingEventManager.receivers.withIndex()) {
            logger.info("${i + 1}. ${j.name}")
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(
            this,
            this::updateServerInfo,
            0L,
            pluginConfig.serverInfoUpdateTime.toLong() * 20
        )
        if (pluginConfig.metrics) Metrics(this, 24055)
    }

    override fun onDisable() {
        messagingEventManager.receivers.forEach { it.onDisable() }
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
        Translator.reload()
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
        messagingEventManager.dispatch(ServerInfoContext(pluginConfig.defaultServerInfoFormat))
    }

    override fun reloadConfig() {
        pluginConfig = ConfigManager.create(MMBConfiguration::class.java) {
            it.withConfigurer(YamlBukkitConfigurer())
            it.withBindFile(File(dataFolder, "config.yml"))
            it.withRemoveOrphans(true)
            it.saveDefaults()
            it.load(true)
        }
    }
}
