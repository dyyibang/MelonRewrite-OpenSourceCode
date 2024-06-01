package dev.zenhao.melon.module

import dev.zenhao.melon.gui.clickgui.new.component.Component
import dev.zenhao.melon.gui.clickgui.new.render.DrawDelegate
import dev.zenhao.melon.gui.clickgui.new.render.DrawScope

open class HudComponent(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 10f,
    height: Float = 10f,
    drawDelegate: DrawDelegate = DrawDelegate.defaultDrawDelegate
) : Component(
    x,
    y,
    width,
    height,
    drawDelegate
) {
    protected var dragging = false
    protected var dragX = 0f
    protected var dragY = 0f

//    final override fun DrawScope.render(mouseX: Float, mouseY: Float) {
//        if (dragging) {
//            try {
//                setPosition(
//                    (mouseX - dragX).coerceIn(0f, AbstractModule.mc.window.scaledWidth - width),
//                    (mouseY - dragY).coerceIn(0f, AbstractModule.mc.window.scaledHeight - height)
//                )
//            } catch (e: IllegalArgumentException) {
//                setPosition(0f, 0f)
//            }
//
//            relativeX = x / AbstractModule.mc.window.scaledWidth.toFloat()
//            relativeY = y / AbstractModule.mc.window.scaledHeight.toFloat()
//        }
//
//        if (isEnabled) {
//            renderOnGui(mouseX, mouseY)
//        }
//    }
}