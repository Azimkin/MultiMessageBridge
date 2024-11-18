package top.azimkin.multiMessageBridge

import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.java.JavaPlugin
import top.azimkin.multiMessageBridge.api.events.AsyncHeadImageProviderRegistrationEvent
import top.azimkin.multiMessageBridge.api.events.ReceiverRegistrationEvent
import top.azimkin.multiMessageBridge.commands.MainCommand
import top.azimkin.multiMessageBridge.listeners.CommonListener
import top.azimkin.multiMessageBridge.metadata.LuckPermsMetadataProvider
import top.azimkin.multiMessageBridge.metadata.NoMetadataProvider
import top.azimkin.multiMessageBridge.metadata.PlayerMetadataProvider
import top.azimkin.multiMessageBridge.metadata.VaultMetadataProvider
import top.azimkin.multiMessageBridge.skins.HeadProviderManager
import top.azimkin.multiMessageBridge.skins.LinkHeadProvider
import top.azimkin.multiMessageBridge.skins.SkinHeadProvider
import top.azimkin.multiMessageBridge.utilities.runBukkitAsync

class MultiMessageBridge : JavaPlugin() {
    companion object {
        lateinit var inst: MultiMessageBridge private set
    }

    lateinit var headProvider: SkinHeadProvider
        private set
    lateinit var metadataProvider: PlayerMetadataProvider
        private set

    override fun onLoad() {
        inst = this
    }

    override fun onEnable() {
        saveDefaultConfig()

        getCommand("multimessagebridge")?.setExecutor(MainCommand)
        getCommand("multimessagebridge")?.tabCompleter = MainCommand

        server.pluginManager.registerEvents(CommonListener, this)

        reload()

        ReceiverRegistrationEvent(MessagingEventManager.get()).callEvent()
        logger.info("RegisteredReceivers: ")
        for ((i, j) in MessagingEventManager.get().receivers.withIndex()) {
            logger.info("${i + 1}. ${j.name}")
        }
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
            headProvider = mgr.createByName(config.getString("heads.provider")!!, config.getString("heads.url")!!)
        }
        setupMetadataProvider()
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
}
