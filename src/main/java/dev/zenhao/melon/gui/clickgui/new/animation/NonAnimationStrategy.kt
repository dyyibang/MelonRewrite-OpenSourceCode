package dev.zenhao.melon.gui.clickgui.new.animation

import dev.zenhao.melon.gui.clickgui.new.component.Component
import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import net.minecraft.client.gui.DrawContext

object NonAnimationStrategy : AnimationStrategy {
    override fun onBind(component: Component) {
        component.setDrawDelegate(DrawDelegate.defaultDrawDelegate)
    }

    override fun onUnbind(component: Component) {
    }

    override fun onOpen() {
    }

    override fun onClose() {
    }

    override fun onRender(drawContext: DrawContext, mouseX: Float, mouseY: Float, component: Component) {
        component.onRender(drawContext, mouseX, mouseY)
    }

    override fun reset() {
    }

    override fun playFinished(): Boolean {
        return true
    }
}