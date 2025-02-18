package dev.zenhao.melon.module.hud

import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.gui.clickgui.new.render.DrawScope
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.modules.client.Colors
import dev.zenhao.melon.utils.math.LagCompensator.globalInfoPingValue
import melon.system.render.newfont.FontRenderers
import melon.utils.chat.ChatUtil
import melon.utils.concurrent.threads.runSafe
import net.minecraft.client.gui.DrawContext

object PingHUD : HUDModule(
    name = "Ping",
    langName = "延迟显示",
    x = 170f,
    y = 170f,
    category = Category.HUD
) {

    override fun onRender(context: DrawContext) {
        runSafe {
            val fontColor = Colors.hudColor.value.rgb
            val privatePingValue = this@runSafe.globalInfoPingValue()
            val finalString = "Ping " + ChatUtil.SECTIONSIGN + "f" + privatePingValue
            FontRenderers.lexend.drawString(context.matrices, finalString, x + 2.0, y + 3.0, fontColor)
            width = FontRenderers.lexend.getStringWidth(finalString) + 4
        }
    }
}