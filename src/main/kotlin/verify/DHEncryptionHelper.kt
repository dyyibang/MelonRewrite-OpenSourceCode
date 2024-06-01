package verify

import java.security.Key
import java.security.KeyPairGenerator
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec

open class DHEncryptionHelper {
    private var encryptionAlgorithm: String = "AES"

    private var publicKey: PublicKey
    private var keyAgreement: KeyAgreement = KeyAgreement.getInstance("ECDH")
    private var sharedKey: ByteArray? = null

    init {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(256)
        val kp = kpg.generateKeyPair()
        publicKey = kp.public
        keyAgreement.init(kp.private)
    }

    fun setReceiverPublicKey(publicKey: PublicKey?) {
        keyAgreement.doPhase(publicKey, true)
        sharedKey = keyAgreement.generateSecret()
    }

    fun encrypt(bytes: ByteArray): ByteArray {
        val key: Key = generateKey()
        val c = Cipher.getInstance(encryptionAlgorithm)
        c.init(Cipher.ENCRYPT_MODE, key)
        val encVal = c.doFinal(bytes)
        return encVal
    }

    fun decrypt(encryptedData: ByteArray): ByteArray {
        val key: Key = generateKey()
        val c = Cipher.getInstance(encryptionAlgorithm)
        c.init(Cipher.DECRYPT_MODE, key)
        return c.doFinal(encryptedData)
    }

    fun getPublicKey(): PublicKey {
        return publicKey
    }

    private fun generateKey(): Key {
        return SecretKeySpec(sharedKey, encryptionAlgorithm)
    }
}