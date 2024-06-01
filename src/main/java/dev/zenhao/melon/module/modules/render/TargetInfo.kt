package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import melon.system.render.graphic.Render2DEngine
import melon.system.render.graphic.Render3DEngine
import melon.system.render.newfont.FontRenderers
import melon.utils.combat.getTarget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import org.joml.Vector4d
import java.awt.Color
import kotlin.math.min

object TargetInfo: Module(
    name = "TargetInfo",
    langName = "攻击目标信息",
    category = Category.RENDER
) {

    private val background by csetting("Background", Color(0, 0, 0, 100))
    private val range by dsetting("Range", 10.0, 0.5, 20.0)
    private val height by fsetting("Height", 50f, 25f, 100f)
    private val width by fsetting("Width", 150f, 100f, 250f)

    private var tgt: PlayerEntity? = null

    init {
        onMotion {
            tgt = getTarget(range)
        }
        
        onRender2D { event ->
            val target = tgt ?: return@onRender2D

            val xt: Double = target.prevX + (target.x - target.prevX) * mc.tickDelta
            val yt: Double = target.prevY + (target.y - target.prevY) * mc.tickDelta
            val zt: Double = target.prevZ + (target.z - target.prevZ) * mc.tickDelta
            var vector = Vec3d(xt, yt + 1.5, zt)

            var position0: Vector4d? = null
            vector = Render3DEngine.worldSpaceToScreenSpace(Vec3d(vector.x, vector.y, vector.z))
            if (vector.z > 0 && vector.z < 1) {
                position0 = Vector4d(vector.x, vector.y, vector.z, 0.0)
                position0.x = vector.x.coerceAtMost(position0.x)
                position0.y = vector.y.coerceAtMost(position0.y)
                position0.z = vector.x.coerceAtLeast(position0.z)
            }

            position0?.let { pos ->
                val x = pos.z.toFloat() + 10
                val y = pos.y.toFloat()

                Render2DEngine.drawRound(event.drawContext.matrices, x, y, width, height, 2.0f, background)

                FontRenderers.cn.drawString(event.drawContext.matrices, target.name.string, x + height + 5, y + 8, Color.WHITE.rgb)

                val healthLength = min(((width - height - 10) * target.health) / 36, (width - height - 10))
                Render2DEngine.drawRound(event.drawContext.matrices, x + height + 5, y + height - 15, healthLength, 10f, 1f, Color(255, 100, 100))

                val entry = connection.getPlayerListEntry(target.gameProfile.name) ?: return@onRender2D
                val head = entry.skinTexture

                event.drawContext.drawTexture(head, x.toInt() + 4, y.toInt() + 4, height.toInt() - 8, height.toInt() - 8,
                    8f, 8f, 8, 8, 64, 64)
            }
        }
    }
    
}