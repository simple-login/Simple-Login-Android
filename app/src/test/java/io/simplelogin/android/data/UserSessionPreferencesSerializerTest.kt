package io.simplelogin.android.data

import io.simplelogin.android.data.datastore.UserSessionPreferences
import io.simplelogin.android.data.datastore.UserSessionPreferencesSerializer
import io.simplelogin.android.data.util.Crypto
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class MockCrypto: Crypto {
    override fun encrypt(bytes: ByteArray) = bytes.reversedArray()
    override fun decrypt(bytes: ByteArray) = bytes.reversedArray()
}

class UserSessionPreferencesSerializerTest {
    private val sut = UserSessionPreferencesSerializer(MockCrypto())

    @Test
    fun serialize() = runTest {
        val session = UserSessionPreferences()

        val outputStream = ByteArrayOutputStream()
        sut.writeTo(session, outputStream)

        val writtenBytes = outputStream.toByteArray()
        val inputStream = ByteArrayInputStream(writtenBytes)
        val readSession = sut.readFrom(inputStream)

        assert(readSession == session)
    }
}