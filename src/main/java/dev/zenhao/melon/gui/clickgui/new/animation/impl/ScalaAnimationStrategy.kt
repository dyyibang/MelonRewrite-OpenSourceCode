package dev.zenhao.melon.gui.clickgui.new.animation.impl

import dev.zenhao.melon.gui.clickgui.new.animation.AbstractAnimationStrategy
import dev.zenhao.melon.gui.clickgui.new.component.Component
import dev.zenhao.melon.gui.clickgui.new.render.Alignment
import dev.zenhao.melon.module.modules.client.UiSetting
import melon.system.util.interfaces.MinecraftWrapper
import net.minecraft.client.gui.DrawContext

class ScalaAnimationStrategy : AbstractAnimationStrategy() {
    override fun onBind(component: Component) {}

    override fun onUnbind(component: Component) {}

    override fun onRender(drawContext: DrawContext, mouseX: Float, mouseY: Float, component: Component) {
        drawContext.matrices.push()
        when (UiSetting.scalaDirection) {
            Alignment.START -> {
                drawContext.matrices.scale(progress, progress, 1f)
            }

            Alignment.CENTER -> {
                val scale = MinecraftWrapper.minecraft.window
                drawContext.matrices.translate(scale.scaledWidth / 2f, scale.scaledHeight / 2f, 0f)
                drawContext.matrices.scale(progress, progress, 1f)
                drawContext.matrices.translate(-scale.scaledWidth / 2f, -scale.scaledHeight / 2f, 0f)
            }

            Alignment.END -> {
                val scale = MinecraftWrapper.minecraft.window
                drawContext.matrices.translate(scale.scaledWidth.toDouble(), scale.scaledHeight.toDouble(), 0.0)
                drawContext.matrices.scale(progress, progress, 1f)
                drawContext.matrices.translate(-scale.scaledWidth.toDouble(), -scale.scaledHeight.toDouble(), 0.0)

            }
        }

        component.onRender(drawContext, mouseX, mouseY)
        drawContext.matrices.pop()
    }
}