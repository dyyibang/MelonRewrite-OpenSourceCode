package dev.zenhao.melon.command.impl

import dev.zenhao.melon.command.Command
import dev.zenhao.melon.command.executor
import melon.utils.chat.ChatUtil

object HelpCommand : Command("help", description = "Print commands description and usage.") {
    init {
        executor {
            val helpMessage = dev.zenhao.melon.command.CommandManager.getHelpMessage()
            ChatUtil.sendMessage(helpMessage)
        }
    }
}