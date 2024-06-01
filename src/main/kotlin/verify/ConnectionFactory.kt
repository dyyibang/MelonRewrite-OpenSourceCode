package verify

interface ConnectionFactory {
    suspend fun createConnection(ip: String, port: Int): Connection
}