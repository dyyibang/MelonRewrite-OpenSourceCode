package dev.zenhao.melon.command.impl

import dev.zenhao.melon.command.Command
import dev.zenhao.melon.command.executor
import dev.zenhao.melon.gui.clickgui.new.MelonClickGui
import dev.zenhao.melon.gui.clickgui.new.MelonHudEditor
import melon.utils.chat.ChatUtil

object ResetUiCommand : Command("resetui", arrayOf("reui"), "Reset ClickGui component positions") {
    init {
        executor {
            MelonClickGui.resetUiComponentPositions()
            MelonHudEditor.resetUiComponentPositions()
            ChatUtil.sendMessage("Reset position successfully")
        }
    }
}