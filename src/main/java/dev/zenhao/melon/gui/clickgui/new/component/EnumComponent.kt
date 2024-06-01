package dev.zenhao.melon.gui.clickgui.new.component

import dev.zenhao.melon.gui.clickgui.new.render.Alignment
import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.gui.clickgui.new.render.DrawScope
import dev.zenhao.melon.gui.clickgui.new.render.Padding
import dev.zenhao.melon.setting.ModeSetting
import melon.system.util.interfaces.DisplayEnum
import java.awt.Color

class EnumComponent(
    val enumSetting: ModeSetting<Enum<*>>,
    parentComponent: Component,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    drawDelegate: DrawDelegate
) : ListComponent(x, y, width, height, drawDelegate, parentComponent, height), Visible {
    init {
        enumSetting.modes.forEachIndexed { index, enum ->
            elements.add(EnumItemComponent(enum, x, y, width, 12f, drawDelegate) {
                enumSetting.setValueByIndex(index)
                setOpenState(false)
            })
        }
        forceSetOpenState(false)
        totalHeight = elements.size * 14f
    }

    override fun isVisible(): Boolean {
        return enumSetting.isVisible()
    }

    override fun DrawScope.render(mouseX: Float, mouseY: Float) {
        val textColor = if (isHoveringOnTitle(mouseX, mouseY)) {
            Color.WHITE.darker()
        } else {
            Color.WHITE
        }

        drawText(
            enumSetting.name, textColor, verticalAlignment = Alignment.CENTER, containerHeight = selfHeight
        )

        drawText(
            if (enumSetting.value is DisplayEnum) (enumSetting.value as DisplayEnum).displayString
            else enumSetting.value.name,
            textColor,
            horizontalAlignment = Alignment.END,
            verticalAlignment = Alignment.CENTER,
            containerHeight = selfHeight
        )

        renderChildElements(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        super.mouseClicked(mouseX, mouseY, button)
        if (isHoveringOnTitle(mouseX, mouseY) && button == 0) {
            enumSetting.forwardLoop()
            parentComponent.rearrange()
            return true
        }

        if (isOpened) {
            return elements.any {
                it.mouseClicked(mouseX, mouseY, button)
            }
        }

        return false
    }

    private class EnumItemComponent(
        private val enum: Enum<*>,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        drawDelegate: DrawDelegate,
        private val onClick: () -> Unit
    ) : Component(x, y, width, height, drawDelegate) {
        override fun DrawScope.render(mouseX: Float, mouseY: Float) {
            val color = if (isHovering(mouseX, mouseY)) {
                Color.WHITE.darker()
            } else {
                Color.WHITE
            }

            drawText(
                if (enum is DisplayEnum) enum.displayString
                else enum.name,
                color,
                Padding(left = 3f),
                horizontalAlignment = Alignment.END,
                verticalAlignment = Alignment.CENTER
            )
        }

        override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
            if (isHovering(mouseX, mouseY) && button == 0) {
                onClick()
                return true
            }
            return false
        }
    }
}