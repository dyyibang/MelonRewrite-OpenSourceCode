package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import melon.utils.concurrent.threads.runSafe

object AutoWalk: Module(
    name = "AutoWalk",
    langName = "自动行走",
    category = Category.MOVEMENT,
    description = "Automatic walking"
) {

    init {
        onMotion {
            mc.options.forwardKey.isPressed = true
        }
    }

    override fun onDisable() {
        runSafe {
            mc.options.forwardKey.isPressed = false
        }
    }

}