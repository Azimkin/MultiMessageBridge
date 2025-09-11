package top.azimkin.multiMessageBridge.services.imagehosting

interface ImageHosting {
    fun uploadImage(data: String): String?
    fun downloadImage(url: String): String?
}