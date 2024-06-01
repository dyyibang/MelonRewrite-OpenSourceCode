package dev.zenhao.melon.module.modules.client

import dev.zenhao.melon.gui.clickgui.new.MelonClickGui
import dev.zenhao.melon.gui.clickgui.new.MelonHudEditor
import dev.zenhao.melon.manager.FileManager.saveAll
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.hud.Image
import melon.utils.concurrent.threads.runSafe
import net.minecraft.client.util.InputUtil

object ClickGui : Module(
    name = "ClickGUI",
    langName = "ClickGUI",
    category = Category.CLIENT,
    keyCode = InputUtil.GLFW_KEY_U,
    visible = true
) {
    var chinese = bsetting("ChineseUI", false)
    var notification by bsetting("Notification", false)
    var chat = bsetting("ToggleChat", true)

    override fun onEnable() {
        if (mc.currentScreen == MelonHudEditor) {
            MelonHudEditor.close()
        }

        runSafe {
            if (mc.currentScreen is MelonClickGui) {
                return
            }

            mc.setScreen(MelonClickGui)

            Image.startTime = System.currentTimeMillis()
        }
    }

    override fun onDisable() {
        runSafe {
            if (mc.currentScreen is MelonClickGui) {
                mc.setScreen(null)
            }
            saveAll()
        }
    }
}
