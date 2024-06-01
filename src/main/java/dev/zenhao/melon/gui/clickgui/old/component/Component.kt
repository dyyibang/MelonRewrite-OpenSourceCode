package dev.zenhao.melon.gui.clickgui.old.component

import net.minecraft.client.MinecraftClient

abstract class Component : IComponent {
    var mc: MinecraftClient = MinecraftClient.getInstance()
    override var x = 0.0
    override var y = 0.0
    override var width = 0.0
    override var height = 0.0
    var isToggled = false
    var isExtended = false
    override fun mouseReleased(mouseX: Double, mouseY: Double, state: Int) {}
    override fun keyTyped(typedChar: Char, keyCode: Int) {}
    override fun close() {}

    override fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= x.coerceAtMost(x + width) && mouseX <= x.coerceAtLeast(x + width) && mouseY >= y.coerceAtMost(y + height) && mouseY <= y.coerceAtLeast(
            y + height
        )
    }
}
