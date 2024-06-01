package dev.zenhao.melon.module.modules.client

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.animations.AnimationFlag
import dev.zenhao.melon.utils.animations.Easing
import melon.utils.concurrent.threads.runSafe

object GameAnimation : Module(name = "GameAnimation", langName = "游戏动画", category = Category.CLIENT) {
    private var hotbarAnimation = AnimationFlag(Easing.OUT_CUBIC, 200.0f)
    var hotbar = bsetting("Hotbar", true)

    override fun onEnable() {
        runSafe {
            val currentPos = player.inventory.selectedSlot * 20.0f
            hotbarAnimation.forceUpdate(currentPos, currentPos)
        }
    }

    fun updateHotbar(): Float {
        return runSafe {
            val currentPos = player.inventory.selectedSlot * 20f
            return hotbarAnimation.getAndUpdate(currentPos)
        } ?: 0f
    }
}