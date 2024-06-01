package dev.zenhao.melon.module.modules.client

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import melon.system.util.interfaces.DisplayEnum

object LoadingMenu: Module(
    name = "LoadingMenu",
    langName = "加载界面",
    category = Category.CLIENT,
    description = "Loading menu"
) {

    val mode = msetting("Mode", LoadingMenuMode.GENSHIN_IMPACT)

    enum class LoadingMenuMode(override val displayName: CharSequence): DisplayEnum {
        XGP("XGP"),
        GENSHIN_IMPACT("Genshin")
    }

}