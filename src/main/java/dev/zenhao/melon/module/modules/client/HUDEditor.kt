package dev.zenhao.melon.module.modules.client

import dev.zenhao.melon.gui.clickgui.new.MelonClickGui
import dev.zenhao.melon.gui.clickgui.new.MelonHudEditor
import dev.zenhao.melon.manager.FileManager.saveAll
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.hud.Image
import melon.utils.Wrapper

object HUDEditor : Module(name = "HUDEditor", langName = "HUD编辑器", category = Category.CLIENT, visible = false) {

    override fun onEnable() {
        if (mc.currentScreen == MelonClickGui) {
            MelonClickGui.close()
        }

        if (Wrapper.player != null && mc.currentScreen !is MelonHudEditor) {
            mc.setScreen(MelonHudEditor)
            Image.startTime = System.currentTimeMillis()
        }
    }

    override fun onDisable() {
        if (mc.currentScreen is MelonHudEditor) {
            mc.setScreen(null)
        }
        saveAll()
    }
}
