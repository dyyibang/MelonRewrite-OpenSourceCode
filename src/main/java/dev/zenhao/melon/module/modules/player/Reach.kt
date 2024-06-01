package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module

object Reach :Module(name = "Reach", langName = "长臂猿", category = Category.PLAYER, description = "reach") {
    val range = fsetting("Range",6.0f,1.0f,6.0f)
}