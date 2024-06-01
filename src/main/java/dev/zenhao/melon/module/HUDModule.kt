package dev.zenhao.melon.module

import dev.zenhao.melon.module.modules.client.HUDEditor
import dev.zenhao.melon.module.modules.client.UiSetting
import dev.zenhao.melon.setting.BooleanSetting
import melon.events.render.Render2DEvent
import melon.events.screen.ResolutionUpdateEvent
import melon.system.event.safeEventListener
import melon.system.render.graphic.Render2DEngine
import net.minecraft.client.gui.DrawContext
import java.awt.Color

open class HUDModule(
    name: String,
    langName: String = "Undefined",
    open var x: Float = 0f,
    open var y: Float = 0f,
    category: Category = Category.HUD,
    description: String = "",
    visible: Boolean = false
) : AbstractModule() {
    open var width: Float = 10f
    open var height: Float = 10f

    private val pinned0 = bsetting("Pinned", true)
    private val pinned by pinned0
    private var relativeX by fsetting("RelativeX", 0f, -1f, 2f).isTrue { false }
    private var relativeY by fsetting("RelativeY", 0f, -1f, 2f).isTrue { false }

    private var lastX = 0f
    private var lastY = 0f

    protected var dragging = false
    protected var dragX = 0f
    protected var dragY = 0f

    init {
        moduleName = name
        moduleCName = langName
        moduleCategory = category
        this.description = description
        this.isVisible = visible

        pinned0.onChange<BooleanSetting> { _, input ->
            if (input) {
                synchronized(this) {
                    relativeX = x / mc.window.scaledWidth
                    relativeY = y / mc.window.scaledHeight
                }
            }
        }

        safeEventListener<ResolutionUpdateEvent> {
            onResolutionChanged()
        }

        safeEventListener<Render2DEvent> {
            if (HUDEditor.isEnabled) {
                if (UiSetting.getThemeSetting().rounded) {
                    Render2DEngine.drawRound(it.drawContext.matrices, x, y, width, height, 2f, Color(0, 0, 0, 50))
                } else {
                    Render2DEngine.drawRect(it.drawContext.matrices, x, y, width, height, Color(0, 0, 0, 50))
                }
            } else {
                renderHud(it.drawContext, 0f, 0f)
            }
        }
    }

    fun renderHud(context: DrawContext, mouseX: Float, mouseY: Float) {
        if (dragging) {
            val gameWidth = mc.window.scaledWidth
            val gameHeight = mc.window.scaledHeight

            x = (mouseX - dragX).coerceIn(0f, gameWidth.toFloat() - width)
            y = (mouseY - dragY).coerceIn(0f, gameHeight.toFloat() - height)

            relativeX = x / gameWidth
            relativeY = y / gameHeight
        }

        onRender(context)
    }

    private fun onResolutionChanged() {
        if (pinned) {
            val gameWidth = mc.window.scaledWidth
            val gameHeight = mc.window.scaledHeight
            var calculatedX: Float
            var calculatedY: Float

            try {
                calculatedX =
                    (gameWidth.toFloat() * relativeX).coerceIn(0f, gameWidth - width)
                calculatedY =
                    (gameHeight.toFloat() * relativeY).coerceIn(0f, gameHeight - height)
            } catch (e: IllegalArgumentException) {
                calculatedX = 0f
                calculatedY = 0f
            }

            if (lastX != calculatedX || lastY != calculatedY) {
                lastX = calculatedX
                lastY = calculatedY

                x = calculatedX
                y = calculatedY
            }
        }
    }

    open fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isHovering(mouseX, mouseY) && button == 0) {
            dragging = true
            dragX = mouseX - x
            dragY = mouseY - y
            return true
        }
        return false
    }

    open fun rearrange() {
    }

    open fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (dragging && button == 0) {
            dragging = false
            return true
        }
        return false
    }

    open fun isHovering(mouseX: Float, mouseY: Float): Boolean {
        return mouseX in x..(x + width) && mouseY in y..(y + height)
    }
}
