package top.azimkin.multiMessageBridge.data

import java.awt.Color
import java.io.File
import java.util.Base64

data class MessageContext(
    val senderName: String,
    val platform: String,
    val senderPlatformId: Long? = null,
    var message: String? = null,
    var messagePlatformId: Long? = null,
    val sticker: String? = null,
    var replyText: String? = null,
    var replyId: Long? = null,
    var replyUser: String? = null,
    val role: String? = null,
    val roleColor: Color? = null,
    var images: List<String> = listOf(),
    val attachment: Boolean = false,
) : BaseContext
