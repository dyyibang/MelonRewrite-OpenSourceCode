package melon.system.antileak.checks

import melon.utils.chat.ChatUtil
import melon.utils.concurrent.threads.runSafe
import java.io.BufferedReader
import java.io.InputStreamReader

object ProcessCheck : AntiLeakCheck {
    private val bannedProcesses = listOf("Wireshark", "mitmproxy")

    override fun isSafe(): Boolean {
        return runCatching {
            val processBuilder = ProcessBuilder("tasklist")
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()

            val inputStream = process.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String?
            var isSafe = true

            while (reader.readLine().also { line = it } != null) {
                line?.let { lines ->
                    if (bannedProcesses.any { it.lowercase() in (lines.lowercase()) }) {
                        runSafe { ChatUtil.sendMessage(lines.lowercase()) }
                        isSafe = false
                        return@let
                    }
                }
            }

            // Close the reader
            reader.close()
            // Wait for the process to finish
            process.waitFor()
            isSafe
        }.getOrElse { false }
    }
}