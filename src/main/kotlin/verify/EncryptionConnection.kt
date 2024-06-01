package verify

import java.net.Socket
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec

open class EncryptionConnection(socket: Socket) : DefaultConnection(socket) {
    protected val dhEncryptionHelper = DHEncryptionHelper()
    protected var state = NONE

    open suspend fun startHandShake() {
        while (state != VERIFIED) {
            when (state) {
                NONE -> {
                    sendByteArray(
                        dhEncryptionHelper.getPublicKey().encoded
                    )
                    state = WAITING_FOR_SERVER_RESPONSE_PUBLIC_KEY
                }

                WAITING_FOR_SERVER_RESPONSE_PUBLIC_KEY -> {
                    val bytes = receiveByteArray()
                    val keySPec = X509EncodedKeySpec(bytes)
                    val publicKey = KeyFactory.getInstance("EC").generatePublic(keySPec)
                    dhEncryptionHelper.setReceiverPublicKey(publicKey)
                    state = VERIFIED
                }
            }
        }
    }

    override suspend fun sendByteArray(bytes: ByteArray) {
        val data = if (state == VERIFIED) dhEncryptionHelper.encrypt(bytes) else bytes
        super.sendByteArray(data)
    }

    override suspend fun receiveByteArray(): ByteArray {
        val data = super.receiveByteArray()
        return if (state == VERIFIED) {
            return dhEncryptionHelper.decrypt(data)
        } else {
            data
        }
    }

    companion object {
        const val NONE = 0
        const val WAITING_FOR_SERVER_RESPONSE_PUBLIC_KEY = 1
        const val VERIFIED = 114514
    }
}