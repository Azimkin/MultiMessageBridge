package top.azimkin.multiMessageBridge.configuration

import java.io.File

abstract class ReceiverConfig(
    val name: String,
    protected val defaults: Map<String, Any>
) {
    abstract val extension: String

    abstract fun set(key: String, value: Any)

    abstract fun isSet(key: String): Boolean

    abstract fun getInt(key: String): Int

    abstract fun getLong(key: String): Long

    abstract fun getStringList(key: String): List<String>

    abstract fun getString(key: String): String?

    abstract fun getTranslation(key: String): String

    abstract fun getFile(): File

    abstract fun getBoolean(key: String): Boolean

    abstract fun save()

    abstract fun reload()
}