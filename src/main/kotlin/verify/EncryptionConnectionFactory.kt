package verify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket

object EncryptionConnectionFactory : ConnectionFactory {

    override suspend fun createConnection(ip: String, port: Int): Connection = withContext(Dispatchers.IO) {
        val socket = Socket(ip, port)
        val connection = EncryptionConnection(socket)
        connection.startHandShake()
        connection
    }
}