package dev.zenhao.melon.gui.clickgui.new

import com.mojang.blaze3d.platform.GlStateManager
import dev.zenhao.melon.gui.clickgui.new.animation.AnimationStrategy
import dev.zenhao.melon.gui.clickgui.new.animation.NonAnimationStrategy
import dev.zenhao.melon.gui.clickgui.new.component.Component
import dev.zenhao.melon.gui.clickgui.new.component.ComponentContainer
import dev.zenhao.melon.module.modules.client.UiSetting
import melon.events.render.Render2DEvent
import melon.system.event.Listener
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import team.exception.melon.graphics.shaders.impl.ParticleShader
import team.exception.melon.graphics.shaders.impl.WindowBlurShader

open class GuiScreen : Screen(Text.empty()) {
    private var buffer: Component? = null
    private var playingCloseAnimation = false

    val container = ComponentContainer()
    val elements = mutableListOf<Component>()

    var animationStrategy: AnimationStrategy = NonAnimationStrategy
        set(value) {
            field.onUnbind(container)
            field = value
            field.onBind(container)
        }

    private var idontknowwhatnameineedtospecify = false

    init {
        Render2DEvent.eventBus.subscribe(Listener(this, (0..1000).random(), 0) {
            val currentScreen = MinecraftClient.getInstance().currentScreen

            if (playingCloseAnimation && currentScreen != this) {
                if (!animationStrategy.playFinished()) {
                    animationStrategy.onRender((it as Render2DEvent).drawContext, 0f, 0f, container)
                } else {
                    playingCloseAnimation = false
                }
            }
        })
    }

    fun resetUiComponentPositions() {
        var offsetX = 5f
        elements.forEach {
            it.setPosition(offsetX, 10f)
            it.rearrange()

            offsetX += it.width + 5f
        }
    }

    override fun shouldPause(): Boolean {
        return false
    }

    override fun onDisplayed() {
        super.onDisplayed()
        animationStrategy.onOpen()
        playingCloseAnimation = false
        container.onDisplayed()
    }

    override fun close() {
        super.close()
        idontknowwhatnameineedtospecify = false
        animationStrategy.onClose()
        playingCloseAnimation = true
    }

    private fun updatePanelIndex() {
        buffer?.let {
            if (elements.isNotEmpty()) {
                val first = elements.last()
                val index = elements.indexOf(it)

                if (index != -1) {
                    elements[elements.size - 1] = it
                    elements[index] = first
                }
            }
            buffer = null
        }
    }

    fun moveToFirstRender(component: Component) {
        if (elements[0] != component) {
            buffer = component
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        updatePanelIndex()
        if (UiSetting.particleRender) {
            WindowBlurShader.render(width.toDouble(), height.toDouble())
            GlStateManager._enableBlend()
            ParticleShader.render()
        }

        val mouseX = mouseX.toFloat()
        val mouseY = mouseY.toFloat()

        animationStrategy.onRender(context, mouseX, mouseY, container)
        animationStrategy.onPostRender(context, mouseX, mouseY, container)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return container.mouseClicked(mouseX.toFloat(), mouseY.toFloat(), button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return container.mouseReleased(mouseX.toFloat(), mouseY.toFloat(), button)
    }

    open fun keyTyped(keyCode: Int): Boolean {
        return container.keyTyped(keyCode)
    }

    final override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val isReceived = keyTyped(keyCode)

        return if (isReceived) {
            true
        } else {
            super.keyPressed(keyCode, scanCode, modifiers)
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, verticalAmount: Double): Boolean {
        if (verticalAmount < 0) {
            elements.forEach {
                it.setPosition(y = it.y - 10)
                it.rearrange()
            }
        } else if (verticalAmount > 0) {
            elements.forEach {
                it.setPosition(y = it.y + 10)
                it.rearrange()
            }
        }
        return super.mouseScrolled(mouseX, mouseY, verticalAmount)
    }
}