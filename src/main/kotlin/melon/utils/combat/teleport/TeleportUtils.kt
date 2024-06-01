package melon.utils.combat.teleport

import melon.system.event.SafeClientEvent
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround
import net.minecraft.util.math.Vec3d

object TeleportUtils {

    fun SafeClientEvent.doTeleport(pos: Vec3d, onGround: Boolean, func: SafeClientEvent.() -> Unit) {
        val lastPos = player.pos
        player.networkHandler.sendPacket(PositionAndOnGround(
            pos.x, pos.y, pos.z, onGround
        ))
        func.invoke(this)
        player.networkHandler.sendPacket(PositionAndOnGround(
            lastPos.x, lastPos.y, lastPos.z, onGround
        ))
    }

    fun SafeClientEvent.doTeleport(pos: Vec3d, onGround: Boolean) {
        player.networkHandler.sendPacket(PositionAndOnGround(
            pos.x, pos.y, pos.z, onGround
        ))
    }

}