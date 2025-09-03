package top.azimkin.multiMessageBridge.data

import java.awt.Color
import java.io.File

data class MessageContext(
    val senderName: String,
    var message: String,
    val platform: String,
    var replyId: Long? = null,
    var replyText: String? = null,
    var replyUser: String? = null,
    var role: String? = null,
    val attachedFiles: List<File> = listOf(),
    val urlAttachments: List<String> = listOf(),
    var roleColor: Color? = null
) : BaseContext
