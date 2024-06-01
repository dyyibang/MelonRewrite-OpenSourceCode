package dev.zenhao.melon.command.impl

import dev.zenhao.melon.command.Command
import dev.zenhao.melon.command.executor
import dev.zenhao.melon.command.literal
import dev.zenhao.melon.command.match
import dev.zenhao.melon.manager.FileManager
import melon.utils.chat.ChatUtil

object ConfigCommand : Command("config", description = "Config Command (save, reload)") {
    init {
        literal {
            match("save") {
                executor {
                    FileManager.saveAll()
                    ChatUtil.sendMessage("Saved Config!")
                }
            }

            match("reload") {
                executor {
                    FileManager.loadAll()
                    ChatUtil.sendMessage("Config Reloaded!")
                }
            }
        }
    }
}