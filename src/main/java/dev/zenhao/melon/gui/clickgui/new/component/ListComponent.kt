package dev.zenhao.melon.gui.clickgui.new.component

import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.gui.clickgui.new.render.DrawScope
import dev.zenhao.melon.utils.animations.AnimationFlag
import dev.zenhao.melon.utils.animations.Easing
import net.minecraft.client.gui.DrawContext

abstract class ListComponent(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    drawDelegate: DrawDelegate,
    val parentComponent: Component,
    protected val selfHeight: Float,
    protected val elementSpacing: Float = 2f,
    duration: Float = 500f,
) : Component(x, y, width, height, drawDelegate) {
    val elements = mutableListOf<Component>()
    var isOpened = false

    protected val animationFlag = AnimationFlag(Easing.OUT_QUAD, duration)

    protected var totalHeight: Float = 0f
    protected var playingAnimation = false

    override fun onRender(context: DrawContext, mouseX: Float, mouseY: Float) {
        refreshHeight()
        super.onRender(context, mouseX, mouseY)
    }

    final override fun onPostRender(context: DrawContext, mouseX: Float, mouseY: Float) {
        super.onPostRender(context, mouseX, mouseY)
        elements.forEach { it.onPostRender(context, mouseX, mouseY) }
    }

    private fun refreshHeight() {
        val targetHeight = if (isOpened) totalHeight else selfHeight
        val animatedHeight = animationFlag.getAndUpdate(targetHeight)
        setSize(height = animatedHeight)

        if (playingAnimation) {
            if (animatedHeight == targetHeight) {
                playingAnimation = false
            }
            parentComponent.rearrange()
        }
    }

    protected fun setOpenState(isOpened: Boolean) {
        playingAnimation = true
        this.isOpened = isOpened
    }

    protected fun forceSetOpenState(isOpened: Boolean) {
        animationFlag.forceUpdate(if (isOpened) totalHeight else selfHeight)
        this.isOpened = isOpened
        setSize(height = selfHeight)
    }

    override fun DrawScope.render(mouseX: Float, mouseY: Float) {
        renderChildElements(mouseX, mouseY)
    }

    protected fun DrawScope.renderChildElements(mouseX: Float, mouseY: Float) {
        if (height != selfHeight) {
            context.enableScissor(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
            elements.forEach {
                it.onRender(context, mouseX, mouseY)
            }
            context.disableScissor()
        }
    }

    override fun rearrange() {
        if (height == selfHeight) {
            return
        }

        var offsetY = selfHeight + elementSpacing
        elements.forEach {
            it.setPosition(x, y + offsetY)
            it.rearrange()
            offsetY += it.height + elementSpacing
        }
        totalHeight = offsetY

        if (!playingAnimation) {
            setSize(height = if (isOpened) totalHeight else selfHeight)
            animationFlag.forceUpdate(totalHeight)
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isHoveringOnTitle(mouseX, mouseY) && button == 1) {
            setOpenState(!isOpened)

            if (isOpened) {
                expand()
            } else {
                reduce()
            }
            return true
        }

        if (isOpened) {
            return elements.filter {
                if (it is Visible) {
                    it.isVisible()
                } else {
                    true
                }
            }.any {
                it.mouseClicked(mouseX, mouseY, button)
            }
        }

        return false
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isOpened) {
            return elements.filter {
                if (it is Visible) {
                    it.isVisible()
                } else {
                    true
                }
            }.any {
                it.mouseReleased(mouseX, mouseY, button)
            }
        }

        return false
    }

    override fun keyTyped(keyCode: Int): Boolean {
        if (isOpened) {
            return elements.filter {
                if (it is Visible) {
                    it.isVisible()
                } else {
                    true
                }
            }.any {
                it.keyTyped(keyCode)
            }
        }

        return false
    }

    protected fun isHoveringOnTitle(mouseX: Float, mouseY: Float): Boolean {
        return mouseX in x..(x + width) && mouseY in y..(y + selfHeight)
    }

    open fun expand() {}

    open fun reduce() {}

    override fun setDrawDelegate(drawDelegate: DrawDelegate) {
        super.setDrawDelegate(drawDelegate)
        elements.forEach { it.setDrawDelegate(drawDelegate) }
    }

    override fun onGuiClose() {
        elements.forEach { it.onGuiClose() }
    }

    override fun onDisplayed() {
        elements.forEach { it.onDisplayed() }
    }
}