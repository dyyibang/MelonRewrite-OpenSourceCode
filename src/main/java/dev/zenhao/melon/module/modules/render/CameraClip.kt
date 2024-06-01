package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module

object CameraClip: Module(name = "CameraClip", langName = "无死角视觉", category = Category.RENDER) {
    var clip by bsetting("Clip", true)
    var distance by isetting("CameraDistance", 4, 0, 20)
}