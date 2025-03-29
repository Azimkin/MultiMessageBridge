package top.azimkin.multiMessageBridge.data

import top.azimkin.multiMessageBridge.platforms.BaseReceiver
import java.awt.Color
import java.io.File

data class MessageContext(
    var senderName: String,
    var message: String,
    var isReply: Boolean,
    val platform: String,
    var reply: String? = null,
    var replyUser: String? = null,
    var role: String? = null,
    var attachedFiles: List<File> = listOf(),
    var urlAttachments: List<String> = listOf(),
    var roleColor: Color? = null
) : BaseContext

fun messageContext(platform: BaseReceiver, block: MessageContext.() -> Unit): MessageContext = MessageContext("undefined", "undefined", false, platform.name).apply(block)