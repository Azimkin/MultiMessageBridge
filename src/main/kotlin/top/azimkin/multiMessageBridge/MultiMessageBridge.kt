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
import top.azimkin.multiMessageBridge.configuration.DatabaseConfig
import top.azimkin.multiMessageBridge.data.ServerInfoContext
import top.azimkin.multiMessageBridge.data.ServerSessionContext
import top.azimkin.multiMessageBridge.listeners.CommonListener
import top.azimkin.multiMessageBridge.metadata.LuckPermsMetadataProvider
import top.azimkin.multiMessageBridge.metadata.NoMetadataProvider
import top.azimkin.multiMessageBridge.metadata.PlayerMetadataProvider
import top.azimkin.multiMessageBridge.metadata.VaultMetadataProvider
import top.azimkin.multiMessageBridge.platforms.MinecraftReceiver
import top.azimkin.multiMessageBridge.providers.skins.HeadProviderManager
import top.azimkin.multiMessageBridge.providers.skins.LinkHeadProvider
import top.azimkin.multiMessageBridge.providers.skins.SkinHeadProvider
import top.azimkin.multiMessageBridge.utilities.DateFormatter
import top.azimkin.multiMessageBridge.utilities.Translator
import top.azimkin.multiMessageBridge.utilities.runBukkitAsync
import top.azimkin.multiMessageBridge.entities.repo.PlatformMappingRepoImpl
import top.azimkin.multiMessageBridge.entities.repo.MessageRepoImpl
import top.azimkin.multiMessageBridge.services.MessageCleanupService
import top.azimkin.multiMessageBridge.services.MessageService
import top.azimkin.multiMessageBridge.services.database.DatabaseManager
import top.azimkin.multiMessageBridge.services.database.DatabaseManagerFactory
import top.azimkin.multiMessageBridge.services.imagehosting.ImageHosting
import top.azimkin.multiMessageBridge.services.imagehosting.ImageHostingFactory
import top.azimkin.multiMessageBridge.providers.username.UsernameProvider
import top.azimkin.multiMessageBridge.providers.username.UsernameProviderImpl
import java.io.File

class MultiMessageBridge : JavaPlugin() {
    companion object {
        lateinit var inst: MultiMessageBridge private set
    }

    init {
        inst = this
    }

    var enabledIn: Long = System.currentTimeMillis(); private set
    lateinit var messagingEventManager: MessagingEventManager private set
    lateinit var headProvider: SkinHeadProvider private set
    lateinit var metadataProvider: PlayerMetadataProvider private set
    lateinit var pluginConfig: MMBConfiguration private set
    lateinit var databaseConfig: DatabaseConfig private set

    lateinit var imageHosting: ImageHosting private set
    lateinit var dbManager: DatabaseManager private set
    lateinit var messageService: MessageService private set
    lateinit var usernameProvider: UsernameProvider private set
    lateinit var messageCleanupService: MessageCleanupService private set
    val uptime: Long get() = System.currentTimeMillis() - enabledIn
    val dateFormatter = DateFormatter { pluginConfig.timeFormat }

    override fun onEnable() {
        dataFolder.mkdir()
        // lol why not
        getCommand("mmb")?.apply {
            setExecutor(MainCommand)
            tabCompleter = MainCommand
        }

        reload()

        dbManager = DatabaseManagerFactory.create(this, databaseConfig)
        dbManager.init()
        val messageDao = dbManager.getMessageDao()
        val mappingDao = dbManager.getMappingDao()

        val messageRepo = MessageRepoImpl(messageDao)
        val mappingRepo = PlatformMappingRepoImpl(mappingDao)

        messageService = MessageService(messageRepo, mappingRepo)
        usernameProvider = UsernameProviderImpl()

        messageCleanupService = MessageCleanupService(this, messageRepo, mappingRepo, databaseConfig)
        messageCleanupService.scheduleCleanupTask()
        imageHosting = ImageHostingFactory.create(pluginConfig)

        messagingEventManager = MessagingEventManagerImpl(messageService, usernameProvider, imageHosting);

        server.pluginManager.registerEvents(CommonListener, this)

        ReceiverRegistrationEvent(messagingEventManager).callEvent()
        messagingEventManager.enable(pluginConfig.enabledReceivers)

        Bukkit.getScheduler().runTaskTimerAsynchronously(
            this,
            this::updateServerInfo,
            0L,
            pluginConfig.serverInfoUpdateTime.toLong() * 20
        )
        if (pluginConfig.metrics) Metrics(this, 24055)
    }

    override fun onDisable() {
        dbManager.disable()
        val mc = messagingEventManager.receivers.first { it is MinecraftReceiver } as MinecraftReceiver
        messagingEventManager.dispatch(mc, ServerSessionContext(false))
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
        databaseConfig = ConfigManager.create(DatabaseConfig::class.java) {
            it.withConfigurer(YamlBukkitConfigurer())
            it.withBindFile(File(dataFolder, "database.yml"))
            it.withRemoveOrphans(true)
            it.saveDefaults()
            it.load(true)
        }
    }
}
