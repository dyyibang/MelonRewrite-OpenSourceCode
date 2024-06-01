package verify

import java.net.Socket

abstract class Connection(
    val socket: Socket,
) {
    val ip = socket.inetAddress.hostAddress

    abstract suspend fun sendByteArray(bytes: ByteArray)

    abstract suspend fun receiveByteArray(): ByteArray

    suspend fun send(string: String) {
        sendByteArray(string.toByteArray())
    }

    suspend fun receive(): String {
        return String(receiveByteArray())
    }

    open fun close() {
        runCatching {
            socket.close()
        }
    }
}