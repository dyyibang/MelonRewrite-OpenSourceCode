package dev.zenhao.melon.gui.clickgui.new.animation.impl

import dev.zenhao.melon.gui.clickgui.new.AlphaAnimationDrawDelegate
import dev.zenhao.melon.gui.clickgui.new.animation.AnimationStrategy
import dev.zenhao.melon.gui.clickgui.new.component.Component
import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import net.minecraft.client.gui.DrawContext

class AlphaAnimationStrategy(
    private val alphaAnimationDrawDelegate: AlphaAnimationDrawDelegate
) : AnimationStrategy {
    override fun onBind(component: Component) {
        component.setDrawDelegate(alphaAnimationDrawDelegate)
    }

    override fun onUnbind(component: Component) {
        component.setDrawDelegate(DrawDelegate.defaultDrawDelegate)
    }

    override fun onOpen() {
        alphaAnimationDrawDelegate.isReverse = false
        reset()
    }

    override fun onClose() {
        alphaAnimationDrawDelegate.isReverse = true
        reset()
    }

    override fun onRender(drawContext: DrawContext, mouseX: Float, mouseY: Float, component: Component) {
        component.onRender(drawContext, mouseX, mouseY)
    }

    override fun onPostRender(drawContext: DrawContext, mouseX: Float, mouseY: Float, component: Component) {
        component.onPostRender(drawContext, mouseX, mouseY)
    }

    override fun reset() {
        alphaAnimationDrawDelegate.resetAnimation()
    }

    override fun playFinished(): Boolean {
        return alphaAnimationDrawDelegate.isAnimationFinished
    }
}