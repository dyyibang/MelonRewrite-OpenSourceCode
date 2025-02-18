package dev.zenhao.melon.gui.clickgui.new.component

import dev.zenhao.melon.gui.clickgui.new.render.Alignment
import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.gui.clickgui.new.render.DrawScope
import dev.zenhao.melon.module.modules.client.UiSetting
import dev.zenhao.melon.setting.NumberSetting
import dev.zenhao.melon.utils.animations.AnimationFlag
import dev.zenhao.melon.utils.animations.Easing
import dev.zenhao.melon.utils.animations.MathUtils
import org.lwjgl.glfw.GLFW
import java.awt.Color

class NumberSlider<T : Number>(
    val numberSetting: NumberSetting<T>,
    private val parentComponent: Component,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    drawDelegate: DrawDelegate,
) : Component(x, y, width, height, drawDelegate), Visible {
    private var dragging = false
    private var waitingForInput = false
    private var buffer = ""

    private val regex = "[0-9]".toRegex()

    private var heightStartTime = System.currentTimeMillis()
    private val heightProgress
        get() = if (dragging) {
            Easing.LINEAR.inc(Easing.toDelta(heightStartTime, 200f))
        } else {
            Easing.LINEAR.dec(Easing.toDelta(heightStartTime, 200f))
        }.coerceIn(0f, 1f)

    private var targetProgress: Float = numberSetting.percent
    private val animationFlag = AnimationFlag(Easing.OUT_QUAD, 400f)

    override fun isVisible(): Boolean {
        return numberSetting.isVisible()
    }

    override fun DrawScope.render(mouseX: Float, mouseY: Float) {
        if (dragging) {
            val percent = ((mouseX - x) / width).coerceIn(0f, 1f)
            targetProgress = percent
            numberSetting.percent = percent
            animationFlag.forceUpdate(percent)
        }

        val progress = animationFlag.getAndUpdate(targetProgress)
        drawRect(
            x,
            y + height - ((height + 1) * heightProgress).coerceAtLeast(1f),
            (width * progress).coerceAtMost(width),
            1f + (height + 1) * heightProgress,
            UiSetting.getThemeSetting().primary
        )

        val textColor = if (isHovering(mouseX, mouseY) || dragging) {
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
            drawText(numberSetting.name, textColor, verticalAlignment = Alignment.CENTER)
            drawText(
                MathUtils.round(numberSetting.value.toFloat(), 2).toString(),
                textColor,
                horizontalAlignment = Alignment.END,
                verticalAlignment = Alignment.CENTER
            )
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isHovering(mouseX, mouseY)) {
            if (button == 0 && !waitingForInput) {
                dragging = true
                heightStartTime = System.currentTimeMillis()
            } else if (button == 1) {
                waitingForInput = !waitingForInput
            } else {
                return false
            }

            return true
        }

        return false
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (dragging && button == 0) {
            dragging = false
            heightStartTime = System.currentTimeMillis()
            return true
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    override fun keyTyped(keyCode: Int): Boolean {
        if (waitingForInput) {
            when (keyCode) {
                GLFW.GLFW_KEY_ENTER -> {
                    numberSetting.setValueFromString(buffer.toFloat())
                    waitingForInput = false
                    buffer = ""
                    targetProgress = numberSetting.percent
                }

                GLFW.GLFW_KEY_BACKSPACE -> {
                    if (buffer.isNotEmpty()) {
                        buffer = buffer.dropLast(1)
                    }
                }

                else -> {
                    val char = GLFW.glfwGetKeyName(keyCode, 0)?.replace("null", "") ?: ""

                    if (char == "-" && buffer.isEmpty()) {
                        buffer += char
                        return true
                    }

                    if (char == "." && !buffer.contains(".")) {
                        buffer += char
                        return true
                    }

                    if (regex.matches(char)) {
                        buffer += char
                    }
                }
            }

            return true
        }

        return false
    }

    override fun onGuiClose() {
        waitingForInput = false
        animationFlag.forceUpdate(0f)
        buffer = ""
    }
}