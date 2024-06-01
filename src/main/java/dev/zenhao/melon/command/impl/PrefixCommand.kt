package dev.zenhao.melon.command.impl

import dev.zenhao.melon.Melon
import dev.zenhao.melon.command.Command
import dev.zenhao.melon.command.any
import dev.zenhao.melon.command.executor
import dev.zenhao.melon.command.literal
import melon.utils.chat.ChatUtil

object PrefixCommand : Command("prefix") {
    init {
        literal {
            any { anyArgs ->
                executor {
                    val prefix = anyArgs.value()
                    Melon.commandPrefix.value = prefix
                    ChatUtil.sendMessage("Prefix Has Been Set To: $prefix")
                }
            }
        }
    }
}