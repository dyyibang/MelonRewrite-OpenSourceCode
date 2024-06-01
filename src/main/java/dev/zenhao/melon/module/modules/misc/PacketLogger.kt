package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import melon.events.PacketEvents
import melon.system.event.safeEventListener
import melon.utils.chat.ChatUtil
import net.minecraft.network.packet.c2s.play.*
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket

object PacketLogger : Module("PacketLogger", langName = "抓包", category = Category.MISC) {
    private var jump by bsetting("Jump", false)
    private var move by bsetting("Move", false)
    private var place by bsetting("Place", false)
    private var jumped = false
    private var height = 0.0

    init {
        safeEventListener<PacketEvents.Send>(Int.MIN_VALUE) { event ->
            if (event.packet is PlayerMoveC2SPacket && move) {
                val packetMode =
                    if (event.packet is PlayerMoveC2SPacket.Full) "Full" else if (event.packet is PlayerMoveC2SPacket.PositionAndOnGround) "PositionAndOnGround" else if (event.packet is PlayerMoveC2SPacket.LookAndOnGround) "LookAndOnGround" else "OnGroundOnly"
                ChatUtil.sendMessage("${packetMode}:${event.packet.x.toInt()} ${event.packet.y.toInt()} ${event.packet.z.toInt()}")
            }
            if (event.packet is PlayerInteractBlockC2SPacket && place) {
                ChatUtil.sendMessage("Pos:" + event.packet.blockHitResult.blockPos.toString())
            }
            if (event.packet is PlayerActionC2SPacket) {
                ChatUtil.sendMessage(event.packet.action.name)
            }
            if (event.packet is PlayerMoveC2SPacket && jumped && jump) {
                if (player.onGround) {
                    jumped = false
                    height = 0.0
                } else {
                    ChatUtil.sendMessage((event.packet.y - height).toString())
                }
            }
        }

        safeEventListener<PacketEvents.Receive> { event ->
            when (event.packet) {
                is PlayerPositionLookS2CPacket -> {
                    if (!move) return@safeEventListener
                    ChatUtil.sendMessage("PosS2C: ${event.packet.x} ${event.packet.y} ${event.packet.z}")
                }
            }
        }

        onMotion {
            if (mc.options.jumpKey.isPressed && jump) {
                jumped = true
                height = player.y
                ChatUtil.sendMessage("Check Enabled!")
            }
        }
    }
}