package verify

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesEncryptHelper(
    key: String,
    iv: String
) {
    private val encodeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    private val decodeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    private val keySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "AES")
    private val ivSpec = IvParameterSpec(iv.toByteArray(Charsets.UTF_8))

    init {
        encodeCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        decodeCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
    }

    fun encrypt(data: ByteArray): ByteArray {
        return encodeCipher.doFinal(data)
    }

    fun decrypt(data: ByteArray): ByteArray {
        return decodeCipher.doFinal(data)
    }
}