package dev.zenhao.melon.gui.clickgui.new.animation

import dev.zenhao.melon.gui.clickgui.new.component.Component
import net.minecraft.client.gui.DrawContext

interface AnimationStrategy {
    fun onBind(component: Component)

    fun onUnbind(component: Component)

    fun onOpen()

    fun onClose()

    fun onRender(drawContext: DrawContext, mouseX: Float, mouseY: Float, component: Component)

    fun onPostRender(drawContext: DrawContext, mouseX: Float, mouseY: Float, component: Component) {
        component.onPostRender(drawContext, mouseX, mouseY)
    }

    fun reset()

    fun playFinished(): Boolean
}