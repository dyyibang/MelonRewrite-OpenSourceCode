package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module

object NoEntityTrace : Module(name = "NoEntityTrace", langName = "无视碰撞箱", category = Category.PLAYER) {
    var pickaxeOnly = bsetting("PickaxeOnly", true)
    var noSword = bsetting("NoSword", true)
}