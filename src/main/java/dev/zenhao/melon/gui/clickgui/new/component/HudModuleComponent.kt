package dev.zenhao.melon.gui.clickgui.new.component

import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.gui.clickgui.new.render.DrawScope
import dev.zenhao.melon.module.HUDModule

class HudModuleComponent(
    val hudModule: HUDModule,
    panel: Panel,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    drawDelegate: DrawDelegate
) : ModuleComponent(
    hudModule, panel, x, y, width, height, drawDelegate
) {
    override fun rearrange() {
        super.rearrange()
        hudModule.rearrange()
    }

    override fun DrawScope.onPostRender(mouseX: Float, mouseY: Float) {
        if (hudModule.isEnabled) {
            hudModule.renderHud(context, mouseX, mouseY)
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        hudModule.mouseClicked(mouseX, mouseY, button)
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        hudModule.mouseReleased(mouseX, mouseY, button)
        return super.mouseReleased(mouseX, mouseY, button)
    }
}