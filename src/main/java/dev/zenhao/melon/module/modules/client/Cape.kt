package dev.zenhao.melon.module.modules.client

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import net.minecraft.util.Identifier

object Cape : Module(
    "Cape", category = Category.CLIENT
) {
    private val capeEnum by msetting("Type", Cape.MELON)

    @Suppress("UNUSED")
    private enum class Cape(
        val path: String
    ) {
        MELON("luna.png"),
//        MELON("melon_cape.png"),
        VAPE("vape_cape.png"),
        Chicken("chicken_cape.png"),
        BadLion("badlion_cape.png"),
        Creeper("creeper_cape.png"),
        Dragon("dragon_cape.png"),
        Elaina("elaina_cape.png"),
        FDP("fdp_cape.png"),
        LUNAR_LIGHT("lunar_light_cape.png"),
        LUNAR_DARK("lunar_dark_cape.png"),
        Mojang("mojang_cape.png"),
        NovoLine("novoline_cape.png"),
        Sagiri("sagiri_cape.png"),
    }

    fun getCape(): Identifier {
        return Identifier("melon", "cape/${(capeEnum as Cape).path}")
    }
}