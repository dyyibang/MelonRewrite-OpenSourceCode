package dev.zenhao.melon.gui.clickgui.new.component

import dev.zenhao.melon.gui.clickgui.new.render.Alignment
import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.gui.clickgui.new.render.DrawScope
import dev.zenhao.melon.setting.StringSetting
import org.lwjgl.glfw.GLFW
import java.awt.Color

class TextFieldComponent(
    val stringSetting: StringSetting,
    private val parentComponent: Component? = null,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    drawDelegate: DrawDelegate,
) : Component(x, y, width, height, drawDelegate), Visible {
    private var waitingForInput = false
    private var buffer = ""

    override fun isVisible(): Boolean {
        return stringSetting.isVisible()
    }

    override fun DrawScope.render(mouseX: Float, mouseY: Float) {
        val textColor = if (isHovering(mouseX, mouseY)) {
            Color.WHITE.darker()
        } else {
            Color.WHITE
        }

        if (waitingForInput) {
            drawText(
                "[${buffer.ifEmpty { "..." }}]",
                textColor,
                horizontalAlignment = Alignment.CENTER,
                verticalAlignment = Alignment.CENTER
            )
        } else {
            drawText(stringSetting.name, textColor, verticalAlignment = Alignment.CENTER)
            drawText(
                stringSetting.value,
                textColor,
                horizontalAlignment = Alignment.END,
                verticalAlignment = Alignment.CENTER
            )
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isHovering(mouseX, mouseY)) {
            if (button == 1) {
                waitingForInput = !waitingForInput
                return true
            }
        }

        return false
    }

    override fun keyTyped(keyCode: Int): Boolean {
        if (waitingForInput) {
            when (keyCode) {
                GLFW.GLFW_KEY_ENTER -> {
                    stringSetting.value = buffer
                    waitingForInput = false
                    buffer = ""
                }

                GLFW.GLFW_KEY_BACKSPACE -> {
                    if (buffer.isNotEmpty()) {
                        buffer = buffer.dropLast(1)
                    }
                }

                else -> {
                    buffer += GLFW.glfwGetKeyName(keyCode, 0)?.replace("null", "") ?: ""
                }
            }
            return true
        }

        return false
    }

    override fun onGuiClose() {
        waitingForInput = false
        buffer = ""
    }
}