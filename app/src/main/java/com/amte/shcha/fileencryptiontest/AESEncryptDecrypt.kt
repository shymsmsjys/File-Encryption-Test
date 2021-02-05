package com.amte.shcha.fileencryptiontest

import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

/**
 * Created by kschoi on 2017-05-29.
 */
object AESEncryptDecrypt {
    //dpfxldldkfdpvmdk
    fun encrypt(key: String, plainInput: String): Unit/*String?*/ {
        try {
            val secureKey: Key = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, secureKey)
            val encryptedData = cipher.doFinal(plainInput.toByteArray(charset("UTF-8")))
//            return toHex(encryptedData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        return null
    }

    fun decrypt(key: String, encryptedInput: String?): String? {
        try {
            val secureKey: Key = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, secureKey)
            val plainData = cipher.doFinal(toByte(encryptedInput))
            return String(plainData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // error from converting java to kotlin
    /*private fun toHex(buf: ByteArray?): String {
        if (buf == null) {
            return ""
        }
        val result = StringBuffer()
        for (b in buf) {
            result.append(Integer.toString((b and 0xF0.toByte()) shr 4, 16))
            result.append(Integer.toString(b and 0x0F, 16))
        }
        return result.toString()
    }*/

    private fun toByte(hexString: String?): ByteArray? {
        var result: ByteArray? = null
        try {
            if (hexString == null) {
                return null
            }
            var length = hexString.length
            require(length % 2 != 1) { "For input string: \"$hexString\"" }
            length /= 2
            result = ByteArray(length)
            for (i in 0 until length) {
                val index = i * 2
                result[i] = hexString.substring(index, index + 2).toShort(16).toByte()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}