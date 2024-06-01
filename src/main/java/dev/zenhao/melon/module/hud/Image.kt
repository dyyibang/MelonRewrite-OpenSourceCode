package dev.zenhao.melon.module.hud

import com.mojang.blaze3d.systems.RenderSystem
import dev.zenhao.melon.gui.clickgui.new.MelonClickGui
import dev.zenhao.melon.gui.clickgui.new.MelonHudEditor
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.modules.client.UiSetting
import dev.zenhao.melon.utils.animations.Easing
import melon.utils.chat.ChatUtil
import net.minecraft.client.gui.DrawContext
import team.exception.melon.MelonIdentifier

object Image : HUDModule(name = "Image", langName = "图片显示", category = Category.HUD, x = 150f, y = 150f) {
    private val mode = msetting("Mode", Mode.Mahiro)
    private val arona = MelonIdentifier("textures/arona.png")
    private val mahiro = MelonIdentifier("textures/mahiro.png")
    private val roxy = MelonIdentifier("textures/roxy.png")
    var startTime = System.currentTimeMillis()
    override fun onRender(context: DrawContext) {
        val img = when (mode.value) {
            Mode.Arona -> arona
            Mode.Roxy -> roxy
            else -> mahiro
        }
        try {
            if (mc.currentScreen is MelonClickGui || mc.currentScreen is MelonHudEditor) {
                RenderSystem.disableBlend()
                width = 302f
                height = 460f
                val progress = Easing.IN_QUAD.inc(Easing.toDelta(startTime, UiSetting.animationLength))
                context.drawTexture(
                    img,
                    x.toInt(),
                    y.toInt(),
                    0F,
                    0F,
                    (302f * progress).toInt(),
                    (460f * progress).toInt(),
                    302,
                    460
                )
                RenderSystem.enableBlend()
            }
        } catch (e: Exception) {
            ChatUtil.sendNoSpamMessage("Image failed!!")
        }
    }

    enum class Mode {
        Arona, Mahiro, Roxy
    }
}