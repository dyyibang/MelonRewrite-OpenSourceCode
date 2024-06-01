package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.TimerUtils
import melon.utils.extension.fastPos
import melon.utils.extension.sendSequencedPacket
import net.minecraft.item.BlockItem
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos

object AirPlace : Module(name = "AirPlace", langName = "空放", category = Category.MISC) {
    private var placeDelay = TimerUtils()

    init {
        onMotion {
            var ray: BlockPos? = null
            if (mc.crosshairTarget is BlockHitResult) {
                ray = (mc.crosshairTarget as BlockHitResult).blockPos
            }
            ray?.let { r ->
                if (placeDelay.tickAndReset(50L)) {
                    if (mc.options.useKey.isPressed) {
                        if (player.mainHandStack.item !is BlockItem) return@onMotion
                        sendSequencedPacket(world) {
                            fastPos(r, false, sequence = it)
                        }
                    }
                }
            }
        }
    }
}