package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.Melon
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import melon.utils.concurrent.threads.runSafe
import net.minecraft.block.CobwebBlock

object FastWeb : Module(name = "FastWeb", "防蜘蛛网", category = Category.MOVEMENT) {

    val mode by msetting("Mode", Mode.Timer)
    val timer by fsetting("Timer", 7.0f, 0.0f, 10.0f)

    init {
        onMotion {
            if (mode == Mode.Timer) {
                if (world.getBlockState(player.blockPos).block is CobwebBlock) {
                    Melon.TICK_TIMER = timer
                } else Melon.TICK_TIMER = 1f
            } else Melon.TICK_TIMER = 1f
        }
    }

    override fun onDisable() {
        runSafe {
            Melon.TICK_TIMER = 1f
        }
    }

    enum class Mode {
        Timer, Vanilla
    }
}