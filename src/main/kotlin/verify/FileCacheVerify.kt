package verify

import kotlinx.coroutines.launch
import melon.utils.concurrent.threads.IOScope
import java.io.File
import java.nio.file.Files
import java.util.concurrent.CountDownLatch

object FileCacheVerify {
    val cacheFile = File("${System.getProperty("user.home")}\\.system_logger.logs")
    val awaitTask = CountDownLatch(1)

    init {
        IOScope.launch {
            while (true) {
                if (awaitTask.count.toInt() != 0) continue
                SocketDebugger.tryReset(!cacheFile.exists() || SocketConnection.hashCode != cacheFile.readText() || isFileEmpty())
                break
            }
        }
    }

    fun isFileEmpty(): Boolean {
        return cacheFile.exists() && Files.size(cacheFile.toPath()) == 0L
    }
}