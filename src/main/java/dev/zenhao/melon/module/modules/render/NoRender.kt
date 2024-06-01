package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module

object NoRender : Module(name = "NoRender", langName = "移除渲染" , category = Category.RENDER, description = "Ignore Some Effects") {
    var noHurtCam = bsetting("NoHurtCam", true)
    val blockLayer = bsetting("BlockLayer", true)
    var totemPops = bsetting("Totem", false)
    var explosions = bsetting("Explosions", true)
    var fog = bsetting("Fog", true)
}