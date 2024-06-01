package verify

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

open class DefaultConnection(socket: Socket) : Connection(socket) {
    private val inputStream = DataInputStream(socket.getInputStream())
    private val outputStream = DataOutputStream(socket.getOutputStream())

    override suspend fun sendByteArray(bytes: ByteArray) = withContext(Dispatchers.IO) {
        //println("SEND ${bytes.toList()}")
        outputStream.writeInt(bytes.size)
        outputStream.write(bytes)
        outputStream.flush()
    }

    override suspend fun receiveByteArray(): ByteArray = withContext(Dispatchers.IO) {
        val size = inputStream.readInt()
        val bytes = ByteArray(size)
        inputStream.read(bytes, 0, size)
        //println("RECEIVED ${bytes.toList()}")
        bytes
    }

    override fun close() {
        inputStream.close()
        outputStream.close()
        socket.close()
    }
}