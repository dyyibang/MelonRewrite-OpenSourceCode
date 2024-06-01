package dev.zenhao.melon.module.hud

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import melon.system.render.graphic.Render2DEngine
import melon.system.render.newfont.FontRenderers
import melon.utils.combat.getTarget
import melon.utils.concurrent.threads.runSafe
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.player.PlayerEntity
import java.awt.Color
import kotlin.math.min

object TargetHUD: HUDModule(
    name = "TargetHUD",
    langName = "目标信息",
    category = Category.HUD
) {

    private val background by csetting("Background", Color(0, 0, 0, 100))
    private val range by dsetting("Range", 10.0, 0.5, 20.0)

    override var height = 50f
    override var width = 150f

    private var tgt: PlayerEntity? = null

    init {
        onMotion {
            tgt = getTarget(range)
        }
    }

    override fun onRender(context: DrawContext) {
        val target = tgt ?: return

        runSafe {
            Render2DEngine.drawRound(context.matrices, x, y, width, height, 2.0f, background)

            FontRenderers.cn.drawString(context.matrices, target.name.string, x + height + 5, y + 8, Color.WHITE.rgb)

            val healthLength = min(((width - height - 10) * target.health) / 36, (width - height - 10))
            Render2DEngine.drawRound(context.matrices, x + height + 5, y + 35, healthLength, 10f, 1f, Color(255, 100, 100))

            val entry = connection.getPlayerListEntry(target.gameProfile.name) ?: return@runSafe
            val head = entry.skinTexture

            context.drawTexture(head, x.toInt() + 4, y.toInt() + 4, height.toInt() - 8, height.toInt() - 8,
                8f, 8f, 8, 8, 64, 64)
        }
    }

}