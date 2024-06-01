package dev.zenhao.melon.module.hud

import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.gui.clickgui.new.render.DrawScope
import dev.zenhao.melon.manager.MovementManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.modules.client.Colors
import melon.system.event.SafeClientEvent
import melon.system.render.newfont.FontRenderers
import melon.utils.chat.ChatUtil
import melon.utils.concurrent.threads.runSafe
import net.minecraft.client.gui.DrawContext
import java.text.DecimalFormat

object SpeedHUD : HUDModule(
    name = "SpeedHUD",
    langName = "移动速度",
    x = 140f,
    y = 160f,
    category = Category.HUD
) {
    private val formatter = DecimalFormat("#.#")

    override fun onRender(context: DrawContext) {
        runSafe {
            val fontColor = Colors.hudColor.value.rgb
            val finalString = "Speed " + ChatUtil.SECTIONSIGN + "f" + speed() + " km/h"
            FontRenderers.lexend.drawString(context.matrices, finalString, x + 2.0, y + 3.0, fontColor)
            width = FontRenderers.lexend.getStringWidth(finalString) + 4
        }
    }

    fun SafeClientEvent.speed(): String {
        val currentTps: Float = mc.renderTickCounter.tickTime / 1000.0f
        return formatter.format(MovementManager.currentPlayerSpeed / currentTps * 3.6)
    }
}