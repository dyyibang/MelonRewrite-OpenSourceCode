package dev.zenhao.melon.gui.clickgui.new.component

import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.gui.clickgui.new.render.DrawScope
import net.minecraft.client.gui.DrawContext

open class Component(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    drawDelegate: DrawDelegate,
) {
    var x: Float = x
        private set

    var y: Float = y
        private set

    var width: Float = width
        private set

    var height: Float = height
        private set

    var drawDelegate: DrawDelegate = drawDelegate
        private set

    private var drawScope = DrawScope(x, y, width, height, drawDelegate)

    private fun updateDrawScope(x: Float, y: Float, width: Float, height: Float) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        drawScope = DrawScope(x, y, width, height, drawDelegate)
    }

    open fun setDrawDelegate(drawDelegate: DrawDelegate) {
        this.drawDelegate = drawDelegate
    }

    fun setPosition(x: Float = this.x, y: Float = this.y) {
        updateDrawScope(x, y, width, height)
    }

    fun setSize(width: Float = this.width, height: Float = this.height) {
        updateDrawScope(x, y, width, height)
    }

    open fun onRender(context: DrawContext, mouseX: Float, mouseY: Float) {
        drawScope.context = context
        drawScope.render(mouseX, mouseY)
    }

    open fun onPostRender(context: DrawContext, mouseX: Float, mouseY: Float) {
        drawScope.context = context
        drawScope.onPostRender(mouseX, mouseY)
    }

    open fun DrawScope.render(mouseX: Float, mouseY: Float) {}

    open fun DrawScope.onPostRender(mouseX: Float, mouseY: Float) {}

    open fun onDisplayed() {
    }

    open fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        return false
    }

    open fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        return false
    }

    open fun keyTyped(keyCode: Int): Boolean {
        return false
    }

    open fun rearrange() {}

    open fun onGuiClose() {}

    open fun isHovering(mouseX: Float, mouseY: Float): Boolean {
        return mouseX in x..(x + width) && mouseY in y..(y + height)
    }
}