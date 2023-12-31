package com.example.message.util

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AESEncryption {
    companion object {
        fun generateKey(): SecretKey {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            return keyGenerator.generateKey()
        }

        fun encrypt(data: ByteArray, secretKey: SecretKey): ByteArray {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            return cipher.doFinal(data)
        }

        fun decrypt(data: ByteArray, secretKey: SecretKey): ByteArray {
            return try {
                val cipher = Cipher.getInstance("AES")
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
                cipher.doFinal(data)
            } catch (e: Exception) {

                e.printStackTrace()
                data
            }
        }

    }
}