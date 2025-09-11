package top.azimkin.multiMessageBridge.services.imagehosting

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class FreeImageHosting : ImageHosting {
    private val client = OkHttpClient()
    private val apiKey = "6d207e02198a847aa98d0a2a901485a5"
    private val apiUrl = "https://freeimage.host/api/1/upload"

    override fun uploadImage(data: String): String? {
        val formBody = FormBody.Builder()
            .add("key", apiKey)
            .add("action", "upload")
            .add("source", data)
            .add("format", "json")
            .build()

        val request = Request.Builder()
            .url(apiUrl)
            .post(formBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code: $response")
                }

                val responseBody = response.body?.string() ?: throw IOException("Empty response body")

                return parseImageUrlFromResponse(responseBody)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun parseImageUrlFromResponse(jsonResponse: String): String {
        val urlPattern = "\"url\":\"([^\"]+)\"".toRegex()
        val successPattern = "\"success\":\\s*\\{".toRegex()

        if (!successPattern.containsMatchIn(jsonResponse)) {
            val errorPattern = "\"status_txt\":\"([^\"]+)\"".toRegex()
            val errorMatch = errorPattern.find(jsonResponse)
            val errorMessage = errorMatch?.groupValues?.get(1) ?: "Unknown error"
            throw IOException("Upload failed: $errorMessage")
        }

        val urlMatch = urlPattern.find(jsonResponse)
        return urlMatch?.groupValues?.get(1)?.replace("\\/", "/")
            ?: throw IOException("Could not find image URL in response")
    }

    override fun downloadImage(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code: $response")
                }

                val responseBody = response.body?.bytes() ?: throw IOException("Empty response body")
                //val contentType = response.header("Content-Type", "image/jpeg") ?: "image/jpeg"

                val base64Image = java.util.Base64.getEncoder().encodeToString(responseBody)
                return base64Image
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}