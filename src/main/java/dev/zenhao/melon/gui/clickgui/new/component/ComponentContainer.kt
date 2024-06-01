package dev.zenhao.melon.gui.clickgui.new.component

import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.module.modules.client.UiSetting
import net.minecraft.client.gui.DrawContext
import java.awt.Color

class ComponentContainer(
    private var fillBackground: Boolean = false,
    var color: Color = UiSetting.getThemeSetting().secondary,
    private val elements: MutableList<Component> = mutableListOf(),
    drawDelegate: DrawDelegate = DrawDelegate.defaultDrawDelegate,
) : Component(0f, 0f, 0f, 0f, drawDelegate), MutableList<Component> by elements {

    override fun setDrawDelegate(drawDelegate: DrawDelegate) {
        super.setDrawDelegate(drawDelegate)
        elements.forEach { it.setDrawDelegate(drawDelegate) }
    }

    override fun onRender(context: DrawContext, mouseX: Float, mouseY: Float) {
        if (fillBackground) {
            val x = elements.maxOf { it.x }
            val y = elements.minOf { it.y }
            val width = elements.maxOf { it.width }
            val height = elements.maxOf { it.height }

            drawDelegate.drawReact(context.matrices, x, y, width, height, color)
        }
        elements.forEach { it.onRender(context, mouseX, mouseY) }
    }

    override fun onPostRender(context: DrawContext, mouseX: Float, mouseY: Float) {
        elements.forEach { it.onPostRender(context, mouseX, mouseY) }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        return elements.any { it.mouseClicked(mouseX, mouseY, button) }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        return elements.any { it.mouseReleased(mouseX, mouseY, button) }
    }

    override fun keyTyped(keyCode: Int): Boolean {
        return elements.any { it.keyTyped(keyCode) }
    }

    override fun onGuiClose() {
        elements.forEach { it.onGuiClose() }
    }

    override fun onDisplayed() {
        elements.forEach { it.onDisplayed() }
    }
}