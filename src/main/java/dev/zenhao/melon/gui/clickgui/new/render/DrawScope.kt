package dev.zenhao.melon.gui.clickgui.new.render

import dev.zenhao.melon.module.modules.client.UiSetting
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.math.MatrixStack
import java.awt.Color

class DrawScope(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val drawDelegate: DrawDelegate
) {
    lateinit var context: DrawContext

    private val matrixStack: MatrixStack
        get() = context.matrices

    val textHeight = drawDelegate.textHeight

    fun getTextWidth(text: String): Float {
        return drawDelegate.getStringWidth(text)
    }

    fun drawRectBySetting(
        x: Float, y: Float, width: Float, height: Float, color: Color, padding: Padding = Padding.Empty
    ) {
        if (UiSetting.getThemeSetting().rounded) {
            drawRoundRect(x, y, width, height, color, padding)
        } else {
            drawRect(x, y, width, height, color, padding)
        }
    }

    fun drawRect(
        x: Float, y: Float, width: Float, height: Float, color: Color, padding: Padding = Padding.Empty
    ) {
        drawDelegate.drawReact(
            matrixStack,
            x + padding.left,
            y + padding.top,
            (width - padding.left - padding.right).coerceAtLeast(0f),
            (height - padding.top - padding.bottom).coerceAtLeast(0f),
            color
        )
    }

    fun drawRoundRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
        padding: Padding = Padding.Empty
    ) {
        drawDelegate.drawRoundRect(
            matrixStack,
            x + padding.left,
            y + padding.top,
            (width - padding.left - padding.right).coerceAtLeast(0f),
            (height - padding.top - padding.bottom).coerceAtLeast(0f),
            4f,
            color
        )
    }

    fun fillScopeBySetting(color: Color) {
        if (UiSetting.getThemeSetting().rounded) {
            drawRoundRect(x, y, width, height, color)
        } else {
            drawRect(x, y, width, height, color)
        }
    }

    fun drawText(
        text: String,
        x: Float,
        y: Float,
        color: Color,
        padding: Padding = Padding.Empty,
        verticalAlignment: Alignment = Alignment.START,
        horizontalAlignment: Alignment = Alignment.START,
        containerHeight: Float = height,
        containerWidth: Float = width
    ) {
        val offsetX = when (horizontalAlignment) {
            Alignment.START -> padding.left
            Alignment.CENTER -> ((containerWidth - drawDelegate.getStringWidth(text)) / 2) + padding.left
            Alignment.END -> containerWidth - drawDelegate.getStringWidth(text) - padding.left
        }

        val offsetY = when (verticalAlignment) {
            Alignment.START -> padding.top
            Alignment.CENTER -> ((containerHeight - textHeight) / 2) + padding.top
            Alignment.END -> containerHeight - textHeight - padding.top
        }

        drawDelegate.drawText(matrixStack, text, x + offsetX, y + offsetY, color)
    }

    fun drawText(
        text: String,
        color: Color,
        padding: Padding = Padding.Empty,
        verticalAlignment: Alignment = Alignment.START,
        horizontalAlignment: Alignment = Alignment.START,
        containerHeight: Float = height,
        containerWidth: Float = width
    ) {
        drawText(text, x, y, color, padding, verticalAlignment, horizontalAlignment, containerHeight, containerWidth)
    }

    fun drawOutlineRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
        padding: Padding = Padding.Empty
    ) {
        drawDelegate.drawOutlineRect(
            matrixStack,
            x + padding.left,
            y + padding.top,
            (width - padding.left - padding.right).coerceAtLeast(0f),
            (height - padding.top - padding.bottom).coerceAtLeast(0f),
            1f,
            color
        )
    }

    fun drawOutlineRoundRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
        padding: Padding = Padding.Empty
    ) {
        drawDelegate.drawOutlineRoundRect(
            matrixStack,
            x + padding.left,
            y + padding.top,
            (width - padding.left - padding.right).coerceAtLeast(0f),
            (height - padding.top - padding.bottom).coerceAtLeast(0f),
            4f,
            9f,
            1f,
            color
        )
    }

    fun drawOutlineRectBySetting(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
        padding: Padding = Padding.Empty
    ) {
        if (UiSetting.getThemeSetting().rounded) {
            drawOutlineRoundRect(x, y, width, height, color, padding)
        } else {
            drawOutlineRect(x, y, width, height, color, padding)
        }
    }
}