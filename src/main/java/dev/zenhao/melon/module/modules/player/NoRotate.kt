package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket

object NoRotate : Module(name = "NoRotate", langName = "防回弹转头", category = Category.PLAYER) {
    init {
        onPacketReceive { event ->
            if (event.packet is PlayerPositionLookS2CPacket) {
                event.packet.yaw = player.getYaw()
                event.packet.pitch = player.getPitch()
            }
        }
    }
}