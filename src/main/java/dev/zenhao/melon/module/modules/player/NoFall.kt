package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.movement.ElytraFly
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket

object NoFall: Module(
    name = "NoFall",
    langName = "无摔落伤害",
    category = Category.PLAYER,
    description = "Prevents fall damage"
) {
    private var pauseOnElyTra = bsetting("ElytraPause", true)
    private val fallDistance by fsetting("FallDistance", 1.7f, 0f, 5f)

    init {
        onPacketSend { event ->
            if (pauseOnElyTra.value && ElytraFly.isEnabled && player.inventory.getArmorStack(2).item == Items.ELYTRA) return@onPacketSend
            if (event.packet is PlayerMoveC2SPacket && player.fallDistance >= fallDistance) {
                event.packet.onGround = true
            }
        }
    }
}
