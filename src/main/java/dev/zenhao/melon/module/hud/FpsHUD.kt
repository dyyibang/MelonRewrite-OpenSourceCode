package dev.zenhao.melon.module.hud

import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.modules.client.Colors
import dev.zenhao.melon.utils.math.FrameRateCounter
import melon.system.render.newfont.FontRenderers
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Formatting

object FpsHUD : HUDModule(name = "FpsHUD", langName = "帧数显示") {
    override fun onRender(context: DrawContext) {
        val text = "FPS " + Formatting.WHITE + FrameRateCounter.fps

        val textWidth = FontRenderers.lexend.getStringWidth(text)
        width = textWidth + 4f

        FontRenderers.lexend.drawString(
                context.matrices,
                text,
                x + 2,
                y + 3,
                Colors.hudColor.value.rgb,
                false
        )
    }
}