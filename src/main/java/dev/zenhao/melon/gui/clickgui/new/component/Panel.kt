package dev.zenhao.melon.gui.clickgui.new.component

import dev.zenhao.melon.gui.clickgui.new.GuiScreen
import dev.zenhao.melon.gui.clickgui.new.render.Alignment
import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.gui.clickgui.new.render.DrawScope
import dev.zenhao.melon.module.AbstractModule
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.modules.client.UiSetting
import team.exception.melon.graphics.shaders.impl.WindowBlurShader
import java.awt.Color

class Panel(
    modules: List<AbstractModule>,
    val category: Category,
    private val guiScreen: GuiScreen,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    drawDelegate: DrawDelegate
) : dev.zenhao.melon.gui.clickgui.new.component.ListComponent(
    x,
    y,
    width,
    height,
    drawDelegate,
    guiScreen.container,
    height,
    elementSpacing = 0f
) {
    private var dragging = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    private val beforeFilterModuleComponents =
        mutableListOf<ModuleComponent>()

    init {
        modules.forEach {
            if (it is HUDModule) {
                beforeFilterModuleComponents.add(
                    HudModuleComponent(
                        it, this, x, y, width, height, drawDelegate
                    )
                )
            } else {
                beforeFilterModuleComponents.add(
                    ModuleComponent(
                        it, this, x, y, width, height, drawDelegate
                    )
                )
            }
        }
        elements.addAll(beforeFilterModuleComponents)
    }

    override fun onDisplayed() {
        rearrange()
        super.onDisplayed()
    }

    fun filterModules(condition: (String) -> Boolean) {
        elements.clear()
        elements.addAll(beforeFilterModuleComponents.filter { condition(it.module.moduleName) })
        playingAnimation = true
        rearrange()
    }

    override fun rearrange() {
        val y = y + 2f
        var offsetY = selfHeight
        elements.forEach {
            it.setPosition(x, y + offsetY)
            it.rearrange()
            offsetY += it.height
        }
        totalHeight = offsetY + 4f

        if (!playingAnimation) {
            setSize(height = if (isOpened) totalHeight else height)
            animationFlag.forceUpdate(totalHeight)
        }
    }

    override fun DrawScope.render(mouseX: Float, mouseY: Float) {
        if (dragging) {
            setPosition(mouseX - dragOffsetX, mouseY - dragOffsetY)
            rearrange()
        }

        fillScopeBySetting(UiSetting.getThemeSetting().secondary)
        val primaryColor = UiSetting.getThemeSetting().primary

        if (UiSetting.getThemeSetting().panelBorder) {
            drawOutlineRectBySetting(
                x, y, width, height, primaryColor
            )
        }

        if (UiSetting.getThemeSetting().fillPanelTitle) {
            drawRectBySetting(x, y, width, selfHeight, primaryColor)
        }

        drawText(
            category.name,
            Color.WHITE,
            verticalAlignment = Alignment.CENTER,
            horizontalAlignment = Alignment.CENTER,
            containerHeight = selfHeight
        )

        renderChildElements(mouseX, mouseY)

        if (UiSetting.getThemeSetting().rect) {
            WindowBlurShader.render(x - 8.0, y - 8.0, width + 16.0, totalHeight + 16.0)
            //Render2DEngine.drawRectBlurredShadow(context.matrices, x - 8f, y - 8f, width + 16f, totalHeight + 16f, 25, Color(primaryColor.red, primaryColor.green, primaryColor.blue, 30))
        }

        if (isHovering(mouseX, mouseY)) {
            guiScreen.moveToFirstRender(this@Panel)
        }
    }

    override fun reduce() {
        elements.forEach {
            it.onGuiClose()
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isHovering(mouseX, mouseY)) {
            if (isHoveringOnTitle(mouseX, mouseY) && button == 0) {
                dragging = true
                dragOffsetX = mouseX - x
                dragOffsetY = mouseY - y
                return true
            }
            guiScreen.moveToFirstRender(this)
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isHovering(mouseX, mouseY)) {
            if (dragging && button == 0) {
                dragging = false
                return true
            }
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }
}