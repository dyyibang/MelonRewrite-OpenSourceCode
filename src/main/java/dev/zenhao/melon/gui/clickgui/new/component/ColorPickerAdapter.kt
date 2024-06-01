package dev.zenhao.melon.gui.clickgui.new.component

import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.gui.clickgui.new.render.DrawScope
import dev.zenhao.melon.gui.clickgui.old.component.ColorPicker
import dev.zenhao.melon.setting.ColorSetting

class ColorPickerAdapter(
    private val colorSetting: ColorSetting,
    private val parentComponent: Component,
    x: Float, y: Float, width: Float, height: Float, drawDelegate: DrawDelegate
) : Component(x, y, width, height, drawDelegate), Visible {
    private val colorPicker = ColorPicker(colorSetting, width.toDouble() + 12, height.toDouble())

    override fun isVisible(): Boolean {
        return colorSetting.isVisible()
    }

    override fun rearrange() {
        colorPicker.x = x.toDouble() - 6
        colorPicker.y = y.toDouble() + 2
    }

    override fun DrawScope.render(mouseX: Float, mouseY: Float) {
        parentComponent.rearrange()
        colorPicker.render(context.matrices, mouseX.toDouble(), mouseY.toDouble())
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        setSize(height = colorPicker.height.toFloat())
        colorPicker.mouseClicked(mouseX.toDouble(), mouseY.toDouble(), button)
        return false
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        setSize(height = colorPicker.height.toFloat())
        colorPicker.mouseReleased(mouseX.toDouble(), mouseY.toDouble(), button)
        return false
    }
}