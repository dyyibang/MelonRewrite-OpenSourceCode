package dev.zenhao.melon.module.hud

import dev.zenhao.melon.Melon
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.modules.client.Colors
import melon.system.render.graphic.Render2DEngine
import melon.system.render.newfont.FontRenderers
import net.minecraft.client.gui.DrawContext

object WaterMarkHUD :
    HUDModule(name = "WaterMarkHUD", langName = "标题显示", category = Category.HUD, x = 100f, y = 100f) {
    private val rainbow = bsetting("Rainbow", false)
    private var speed by isetting("Speed", 18, 2, 54)
    private var saturation by fsetting("Saturation", 0.65f, 0.0f, 1.0f).isTrue(rainbow)
    private var brightness by fsetting("Brightness", 1.0f, 0.0f, 1.0f).isTrue(rainbow)
    private val fonts by msetting("Fonts", FontMode.Comfortaa)
    override fun onRender(context: DrawContext) {
        val fontColor = if (!rainbow.value) Colors.hudColor.value.rgb else Render2DEngine.rainbow(
            speed,
            0,
            saturation,
            brightness,
            1f
        ).rgb
        val text = when (Melon.userState) {
            Melon.UserType.Beta -> "Melon Beta ${Melon.VERSION}"
            Melon.UserType.User -> "MelonRewrite ${Melon.VERSION}"
            Melon.UserType.Nigger -> "FUCK U NIGGER"
            Melon.UserType.None -> ""
        }
        val font = when (fonts) {
            FontMode.Knight -> FontRenderers.knight
            FontMode.Badaboom -> FontRenderers.badaboom
            else -> FontRenderers.comfortaa
        }
        font.drawString(context.matrices, text, x + 2.0, y + 3.0, fontColor)
        width = font.getStringWidth(text) + 4
        height = font.getFontHeight(text) + 4
    }

    enum class FontMode {
        Comfortaa, Knight, Badaboom,
    }
}