package melon.system.antileak

import kotlinx.coroutines.launch
import melon.system.antileak.checks.AntiLeakCheck
import melon.system.antileak.checks.PackageCheck
import melon.system.antileak.checks.ProcessCheck
import melon.utils.concurrent.threads.IOScope
import verify.SocketDebugger.tryReset

object AntiLeak {
    private val needCheck = mutableListOf<AntiLeakCheck>()

    init {
        needCheck.add(ProcessCheck)
        needCheck.add(PackageCheck)
    }

    fun init() {
        IOScope.launch {
            while (true) {
                if (tryReset(needCheck.any { it.isNotSafe() })) break
            }
        }
    }
}
