package com.example.message.util

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES {
    private val KEY_SIZE = 128
    private val DATA_LENGTH = 128
    private var encryptionCipher: Cipher? = null
    private var key: SecretKey? = null

    constructor()

    constructor(key: String) {
        val temp = key.toByteArray(Charsets.UTF_8)
        key =  SecretKeySpec(temp, "AES")

    }
    @Throws(Exception::class)
    fun init() {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(KEY_SIZE)
        key = keyGenerator.generateKey()
    }
    fun getKey(): SecretKey {
        return key!!;
    }
    private fun encode(data: ByteArray): String {
        return Base64.getEncoder().encodeToString(data)
    }

    private fun decode(data: String): ByteArray {
        return Base64.getDecoder().decode(data)
    }

    @Throws(Exception::class)
    fun encrypt(data: String): String {
        val dataInBytes = data.toByteArray()
        encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding")

        encryptionCipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = encryptionCipher.doFinal(dataInBytes)
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    @Throws(Exception::class)
    fun decrypt(encryptedData: String): String {
        val dataInBytes = decode(encryptedData)
        val decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(DATA_LENGTH, encryptionCipher!!.iv)
        decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec)
        val decryptedBytes = decryptionCipher.doFinal(dataInBytes)
        return String(decryptedBytes)
    }
}
