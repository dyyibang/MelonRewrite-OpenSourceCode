package verify

import dev.zenhao.melon.Melon
import dev.zenhao.melon.Melon.Companion.verifiedState
import dev.zenhao.melon.module.ModuleManager
import kotlinx.coroutines.launch
import melon.utils.concurrent.threads.IOScope
import verify.SocketConnection.DOMAIN
import verify.SocketConnection.DOMAINDEBUG
import verify.SocketConnection.connection

object SocketDebugger {
    private val illegalArray = arrayOf(
        "127.0.0.1", "localhost", "local", "0.0.0.0", "1.1.1.1"
    )
    var initState = 0

    fun init() = runCatching {
        if (initState > 0) {
            tryReset(true)
            return@runCatching
        }
        initState++
        IOScope.launch {
//            launch(Dispatchers.IO) {
//                while (true) {
//                    if (breakLoop) break
//                    tryReset(FileCacheVerify.isFileEmpty()) {
//                        breakLoop = true
//                    }
//                }
//            }
            //connection.socket.sendUrgentData(0)
            while (true) {
                if (tryReset(connection.socket.inetAddress.hostName != DOMAINDEBUG || illegalArray.any { it == DOMAIN } || illegalArray.any { it == DOMAINDEBUG })) break
            }
        }
    }.onFailure {
        tryReset(true)
    }

    fun tryReset(actionNeeded: Boolean, block: (() -> Unit)? = null): Boolean {
        return if (!actionNeeded) false else {
            connection.close()
            verifiedState = -1
            Melon.userState = Melon.UserType.Nigger
            ModuleManager.moduleList.clear()
            block?.invoke()
            true
        }
    }
}