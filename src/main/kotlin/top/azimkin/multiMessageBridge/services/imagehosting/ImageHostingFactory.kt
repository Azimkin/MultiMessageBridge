package top.azimkin.multiMessageBridge.services.imagehosting
import top.azimkin.multiMessageBridge.configuration.MMBConfiguration

class ImageHostingFactory {
    companion object {
        fun create(config: MMBConfiguration): ImageHosting {
            return when (val databaseType = config.imageHosting.type.lowercase()) {
                "freeimage" -> createFreeImageHosting()
                else -> throw IllegalArgumentException("Unsupported database type: $databaseType")
            }
        }

        private fun createFreeImageHosting(): FreeImageHosting {
            return FreeImageHosting()
        }
    }
}