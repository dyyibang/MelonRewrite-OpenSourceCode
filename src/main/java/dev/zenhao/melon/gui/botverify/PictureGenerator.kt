package dev.zenhao.melon.gui.botverify

import com.mojang.blaze3d.systems.RenderSystem
import dev.zenhao.melon.utils.math.RandomUtil
import melon.system.render.graphic.Render2DEngine
import melon.system.render.newfont.FontRenderers
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import team.exception.melon.MelonIdentifier
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object PictureGenerator : Screen(Text.literal("谁是杨超亲爹")) {
    private var adjustedMap = ConcurrentHashMap<PictureHover, String>()
    private var addedImageList = CopyOnWriteArrayList<String>()
    private var imageList = CopyOnWriteArrayList<Picture>()
    private var images = arrayListOf(
        ""
//        "verify/1.png",
//        "verify/2.png",
//        "verify/3.png",
//        "verify/4.png",
//        "verify/5.png",
//        "verify/6.png",
//        "verify/7.png",
//        "verify/8.png",
//        "verify/9.png",
//        "verify/10.png",
//        "verify/11.png",
//        "verify/12.png",
//        "verify/13.png",
//        "verify/14.png"
    )
    private var hoveredState = "null"
    private var currentTime = System.currentTimeMillis()
    private var imageScale = 0.35f
    private var shuffled = false

    //如果是Dev版本就改成"正数" User版本就改成负数
    var realAdded = false
    var verifiedState = -1

    fun reset() {
        adjustedMap.clear()
        addedImageList.clear()
        imageList.clear()
        realAdded = false
        shuffled = false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        adjustedMap.forEach { (pic, _) ->
            //val image = NativeImage.read(Melon.Companion::class.java.getResourceAsStream("/assets/melon/${pic.imagePath}"))
            //ChatUtil.sendMessage("${pic.imagePath}: ${pic.x} ${pic.y} ${pic.width.toDouble()} ${pic.height.toDouble()}")
            if (Render2DEngine.isHovered(
                    mouseX,
                    mouseY,
                    pic.x.toDouble(),
                    pic.y.toDouble(),
                    pic.width.toDouble(),
                    pic.height.toDouble()
                ) && pic.imageID == 12010915
            ) {
                verifiedState = RandomUtil.nextInt(1, Int.MAX_VALUE)
                //ChatUtil.sendMessage(pic.imagePath)
                MinecraftClient.getInstance().setScreen(null)
                return@forEach
            }
        }
        return false
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val scaledScreen = MinecraftClient.getInstance().window
        val x1 = scaledScreen.scaledWidth / 4
        val x2 = scaledScreen.scaledWidth * 3 / 4
        val y2 = scaledScreen.scaledHeight / 4 + (x2 - x1) / 2
        val imageSizeFactor = 2.5f
        if (!realAdded) {
            val picture = Picture(x2, y2, "verify/real.png")
            imageList.add(picture)
            realAdded = true
        }
        whileLoop@ while (imageList.size < 4) {
            imageLoop@ for (path in images.shuffled()) {
                val pic = Picture(x2, y2, path)
                if (addedImageList.contains(pic.imagePath)) continue@imageLoop
                if (imageList.size >= 4) break@whileLoop
                imageList.add(pic)
                addedImageList.add(pic.imagePath)
            }
        }
        if (!shuffled) {
            imageList = CopyOnWriteArrayList(imageList.shuffled())
            shuffled = true
        }
        Render2DEngine.drawRect(
            context.matrices,
            scaledScreen.x.toFloat(),
            0f,
            scaledScreen.scaledWidth.toFloat(),
            scaledScreen.scaledHeight.toFloat(),
            Color.BLACK
        )
        context.matrices.push()
        context.matrices.scale(imageSizeFactor, imageSizeFactor, imageSizeFactor)
        FontRenderers.cn.drawString(
            context.matrices,
            "请问下列哪个是杨超的爹?",
            scaledScreen.scaledWidth / (2.5 * imageSizeFactor),
            scaledScreen.scaledHeight / (9.0 * imageSizeFactor),
            Color.WHITE.rgb
        )
        context.matrices.pop()
        adjustedMap.forEach { (picHover, _) ->
            if (picHover.imagePath == hoveredState) {
                Render2DEngine.drawRect(
                    context.matrices,
                    (picHover.x - 10f),
                    (picHover.y - 10f),
                    (picHover.width + 20f),
                    (picHover.height + 20f),
                    Color.RED
                )
                return@forEach
            }
        }
        var sortedX = 0
        var sortedY = 0
        var sequence = 0
        context.matrices.push()
        context.matrices.scale(imageScale, imageScale, imageScale)
        imageList.forEach { pic ->
            val imageX = pic.x + sortedX
            val imageY = pic.y + sortedY
            val adjustedPic = PictureHover(
                (imageX * imageScale).toInt(),
                (imageY * imageScale).toInt(),
                (x2 * imageScale).toInt(),
                (y2 * imageScale).toInt(),
                pic.imagePath,
                if (pic.imagePath == "verify/real.png") 12010915 else pic.imagePath.hashCode()
            )
            if (!adjustedMap.containsValue(pic.imagePath)) {
                adjustedMap[adjustedPic] = pic.imagePath
            }
            if (Render2DEngine.isHovered(
                    mouseX.toDouble(),
                    mouseY.toDouble(),
                    adjustedPic.x.toDouble(),
                    adjustedPic.y.toDouble(),
                    adjustedPic.width.toDouble(),
                    adjustedPic.height.toDouble()
                ) && hoveredState != pic.imagePath
            ) {
                currentTime = System.currentTimeMillis()
                hoveredState = pic.imagePath
            }
            Render2DEngine.drawTexture(
                context,
                MelonIdentifier(pic.imagePath),
                imageX,
                imageY,
                x2,
                y2
            )
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableBlend()
            RenderSystem.depthMask(true)
            RenderSystem.enableDepthTest()
            sequence++
            when (sequence) {
                1 -> {
                    sortedX = pic.x + (pic.x / 6)
                    sortedY = 0
                }

                2 -> {
                    sortedX = 0
                    sortedY = pic.y + (pic.y / 6)
                }

                3 -> {
                    sortedX = pic.x + (pic.x / 6)
                }
            }
        }
        context.matrices.pop()
    }

    class Picture(var x: Int, var y: Int, val imagePath: String)

    class PictureHover(var x: Int, var y: Int, var width: Int, val height: Int, val imagePath: String, val imageID: Int)
}