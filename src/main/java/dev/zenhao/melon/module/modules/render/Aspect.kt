package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module

object Aspect : Module(name = "Aspect", langName = "视角", description = "Set Aspect", category = Category.RENDER) {
    val ratio by fsetting("Aspect", 1.0f, 0.1f, 2.5f)
}