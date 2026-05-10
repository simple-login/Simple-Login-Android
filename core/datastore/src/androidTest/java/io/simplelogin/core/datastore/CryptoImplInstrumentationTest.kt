package io.simplelogin.core.datastore

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoImplInstrumentationTest {
    private lateinit var sut: Crypto

    @Before
    fun setUp() {
        sut = CryptoImpl
    }

    @Test
    fun encryptAndDecrypt() {
        val text = "Random string"
        val encrypted = sut.encrypt(text.toByteArray(Charsets.UTF_8))
        val decrypted = sut.decrypt(encrypted)
        assert(text == decrypted.toString(Charsets.UTF_8))
    }
}
