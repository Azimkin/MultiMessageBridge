package top.azimkin.multiMessageBridge

import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import top.azimkin.mmb.Metrics
import top.azimkin.multiMessageBridge.api.events.ImplementationsRegistrationEvent
import top.azimkin.multiMessageBridge.commands.MainCommand
import top.azimkin.multiMessageBridge.configuration.DatabaseConfig
import top.azimkin.multiMessageBridge.configuration.MMBConfiguration
import top.azimkin.multiMessageBridge.data.ServerInfoContext
import top.azimkin.multiMessageBridge.data.ServerSessionContext
import top.azimkin.multiMessageBridge.entities.repo.MessageRepoImpl
import top.azimkin.multiMessageBridge.entities.repo.PlatformMappingRepoImpl
import top.azimkin.multiMessageBridge.listeners.CommonListener
import top.azimkin.multiMessageBridge.metadata.LuckPermsMetadataProvider
import top.azimkin.multiMessageBridge.metadata.NoMetadataProvider
import top.azimkin.multiMessageBridge.metadata.PlayerMetadataProvider
import top.azimkin.multiMessageBridge.metadata.VaultMetadataProvider
import top.azimkin.multiMessageBridge.platforms.BaseReceiver
import top.azimkin.multiMessageBridge.platforms.MinecraftReceiver
import top.azimkin.multiMessageBridge.providers.skins.LinkHeadProvider
import top.azimkin.multiMessageBridge.providers.skins.SkinHeadProvider
import top.azimkin.multiMessageBridge.providers.username.UsernameProvider
import top.azimkin.multiMessageBridge.providers.username.UsernameProviderImpl
import top.azimkin.multiMessageBridge.services.MessageCleanupService
import top.azimkin.multiMessageBridge.services.database.DatabaseManager
import top.azimkin.multiMessageBridge.services.database.DatabaseManagerFactory
import top.azimkin.multiMessageBridge.services.imagehosting.FreeImageHosting
import top.azimkin.multiMessageBridge.services.imagehosting.ImageHosting
import top.azimkin.multiMessageBridge.services.message.BlankMessageService
import top.azimkin.multiMessageBridge.services.message.MessageService
import top.azimkin.multiMessageBridge.services.message.MessageServiceWithOrmLite
import top.azimkin.multiMessageBridge.utilities.DateFormatter
import top.azimkin.multiMessageBridge.utilities.Translator
import top.azimkin.multiMessageBridge.utilities.runSync
import java.io.File

class MultiMessageBridge : JavaPlugin() {
    companion object {
        lateinit var inst: MultiMessageBridge private set
    }

    init {
        inst = this
        dataFolder.mkdir()
    }

    var enabledIn: Long = System.currentTimeMillis(); private set
    lateinit var implementationRegistry: ImplementationRegistry private set
    lateinit var messagingEventManager: MessagingEventManager private set
    lateinit var headProvider: SkinHeadProvider private set
    lateinit var metadataProvider: PlayerMetadataProvider private set
    lateinit var pluginConfig: MMBConfiguration private set

    lateinit var imageHosting: ImageHosting private set
    lateinit var dbManager: DatabaseManager private set
    lateinit var messageService: MessageService private set
    lateinit var usernameProvider: UsernameProvider private set
    lateinit var messageCleanupService: MessageCleanupService private set
    val uptime: Long get() = System.currentTimeMillis() - enabledIn
    val dateFormatter = DateFormatter { pluginConfig.timeFormat }
    private var serverInfoUpdateTask: BukkitTask? = null

    override fun onEnable() {
        // lol why not
        getCommand("mmb")?.apply {
            setExecutor(MainCommand)
            tabCompleter = MainCommand
        }
        server.pluginManager.registerEvents(CommonListener, this)

        reload()

        if (pluginConfig.metrics) Metrics(this, 24055)
    }

    override fun onDisable() {
        serverInfoUpdateTask?.cancel()
        doIfInitialized { dbManager.disable() }
        doIfInitialized {
            val mc = messagingEventManager.receivers.first { it is MinecraftReceiver } as MinecraftReceiver
            messagingEventManager.dispatch(mc, ServerSessionContext(false))
        }
        doIfInitialized { messagingEventManager.receivers.forEach { it.onDisable() } }
    }

    fun reload() {
        onDisable()

        reloadConfig()
        if (pluginConfig.enableMessageStorage) {
            initDatabase()
        } else {
            messageService = BlankMessageService()
        }

        implementationRegistry = ImplementationRegistryImpl()
        implementationRegistry.register(
            "default",
            SkinHeadProvider::class.java
        ) { LinkHeadProvider(pluginConfig.heads.url) }
        implementationRegistry.register("default", UsernameProvider::class.java) { UsernameProviderImpl() }

        val action = {
            ImplementationsRegistrationEvent(implementationRegistry).callEvent()

            headProvider = implementationRegistry
                .getImplementation(SkinHeadProvider::class.java, pluginConfig.heads.provider)
                ?: LinkHeadProvider(pluginConfig.heads.url)
            usernameProvider = implementationRegistry
                .getImplementation(
                    UsernameProvider::class.java,
                    pluginConfig.advanced.implementations["username"] ?: "default"
                )
                ?: UsernameProviderImpl()
            imageHosting = implementationRegistry
                .getImplementation(ImageHosting::class.java, pluginConfig.imageHosting.type)
                ?: FreeImageHosting()

            messagingEventManager = MessagingEventManagerImpl(messageService, usernameProvider, imageHosting)

            val receivers = implementationRegistry.getImplementations(BaseReceiver::class.java)
            messagingEventManager.register(*receivers.entries.map { e -> e.key to { e.value.get() } }.toTypedArray())
            messagingEventManager.enable(pluginConfig.enabledReceivers)

            setupMetadataProvider()
            Translator.reload()

            serverInfoUpdateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                this,
                this::updateServerInfo,
                0L,
                pluginConfig.serverInfoUpdateTime.toLong() * 20
            )
        }
        if (Bukkit.isPrimaryThread()) action()
        else runSync(action)
    }

    fun initDatabase() {
        val config = ConfigManager.create(DatabaseConfig::class.java) {
            it.withConfigurer(YamlBukkitConfigurer())
            it.withBindFile(File(dataFolder, "database.yml"))
            it.withRemoveOrphans(true)
            it.saveDefaults()
            it.load(true)
        }

        dbManager = DatabaseManagerFactory.create(this, config)
        dbManager.init()
        val messageDao = dbManager.getMessageDao()
        val mappingDao = dbManager.getMappingDao()

        val messageRepo = MessageRepoImpl(messageDao)
        val mappingRepo = PlatformMappingRepoImpl(mappingDao)

        messageService = MessageServiceWithOrmLite(messageRepo, mappingRepo)

        messageCleanupService = MessageCleanupService(this, messageRepo, mappingRepo, config)
        messageCleanupService.scheduleCleanupTask()
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

    fun doIfInitialized(action: () -> Unit) {
        try {
            action()
        } catch (_: Throwable) {
        }
    }
}
