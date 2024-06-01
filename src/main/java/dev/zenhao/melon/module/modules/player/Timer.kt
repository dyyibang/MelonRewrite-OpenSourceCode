package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.Melon
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module

object Timer : Module(name = "Timer", langName = "变速齿轮", category = Category.PLAYER) {
    private val speed = fsetting("Speed", 1.2f, 1f, 10f)

    init {
        onMotion {
            Melon.TICK_TIMER = speed.value
        }
    }

    override fun onDisable() {
        Melon.TICK_TIMER = 1f
    }
}