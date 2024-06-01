package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket

object XCarry: Module(
    name = "XCarry",
    langName = "物品栏存储",
    description = "Allows you to store four extra item stacks in your crafting grid",
    category = Category.PLAYER
) {

    init {

        onPacketSend {
            if (it.packet !is CloseHandledScreenC2SPacket) return@onPacketSend
            if (it.packet.syncId == player.playerScreenHandler.syncId) {
                it.cancel()
            }
        }

    }

}