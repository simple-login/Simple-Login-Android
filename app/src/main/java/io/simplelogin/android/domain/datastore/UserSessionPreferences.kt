package io.simplelogin.android.domain.datastore

import androidx.datastore.core.Serializer
import io.simplelogin.android.domain.util.Crypto
import io.simplelogin.android.domain.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64
import javax.inject.Inject

@Serializable
data class UserSessionPreferences(
    val baseUrl: String = Constants.DEFAULT_BASE_URL,
    val apiKey: String? = null
)

class UserSessionPreferencesSerializer @Inject constructor(val crypto: Crypto)
    : Serializer<UserSessionPreferences> {
    override val defaultValue: UserSessionPreferences
        get() = UserSessionPreferences()

    override suspend fun readFrom(input: InputStream): UserSessionPreferences {
        val encryptedBytes = withContext(Dispatchers.IO) {
            input.use { it.readBytes() }
        }
        val encryptedBytesDecoded = Base64.getDecoder().decode(encryptedBytes)
        val decryptedBytes = crypto.decrypt(encryptedBytesDecoded)
        val decodedJsonString = decryptedBytes.decodeToString()
        return Json.decodeFromString(decodedJsonString)
    }

    override suspend fun writeTo(t: UserSessionPreferences, output: OutputStream) {
        val json = Json.encodeToString(t)
        val bytes = json.toByteArray()
        val encryptedBytes = crypto.encrypt(bytes)
        val encryptedBytesBase64 = Base64.getEncoder().encode(encryptedBytes)
        withContext(Dispatchers.IO) {
            output.use {
                it.write(encryptedBytesBase64)
            }
        }
    }
}