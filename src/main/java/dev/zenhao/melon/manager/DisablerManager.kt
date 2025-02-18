package dev.zenhao.melon.manager

import dev.zenhao.melon.module.modules.combat.Burrow
import dev.zenhao.melon.utils.extension.sq
import melon.events.PacketEvents
import melon.events.player.PlayerMotionEvent
import melon.system.event.AlwaysListening
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.utils.TickTimer
import melon.utils.chat.ChatUtil
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.minecraft.util.math.BlockPos
import kotlin.math.abs
import kotlin.math.sqrt

object DisablerManager : AlwaysListening {
    var timerFlagData: TimerFlag? = null
    private val timerCalculator = TickTimer()

    class TimerFlag(
        val tickTimer: TickTimer,
        val recordTime: Long,
        val playerPos: BlockPos,
        val distanceToFlagPos: Double,
        val flagPacket: PlayerMoveC2SPacket.Full,
        var flagVL: Int = 0
    )

    fun SafeClientEvent.flagPacket(time: Long, playerPos: BlockPos) {
        timerFlagData =
            TimerFlag(TickTimer(), time, playerPos, sqrt((playerPos.x.sq + playerPos.y.sq + playerPos.z.sq).toDouble()), PlayerMoveC2SPacket.Full(player.x, -1337.0, player.z, player.yaw, player.pitch, false))
        resetTimer()
    }

    private fun resetTimer() {
        timerCalculator.reset()
    }

    fun onInit() {
        safeEventListener<PlayerMotionEvent>(true) {
            timerFlagData?.let { timerFlag ->
                //Player Pos Resync From SpoofPosition
                if (abs(sqrt((player.x.sq + player.y.sq + player.z.sq) - timerFlag.distanceToFlagPos)) > 16) {
                    connection.sendPacket(
                        PlayerMoveC2SPacket.Full(
                            player.x, player.y, player.z, player.yaw, player.pitch, player.onGround
                        )
                    )
                    ChatUtil.sendNoSpamMessage("[Disabler] PlayerPos Synced")
                }
                if (Burrow.forcePacket && timerCalculator.tick(400) && timerFlag.flagVL < 1) {
                    //Invalid Packet Flag
                    repeat(2) { connection.sendPacket(timerFlag.flagPacket) }
                    ChatUtil.sendNoSpamMessage("[Disabler] ForcePacket Sent")
                }
            }
        }

        safeEventListener<PacketEvents.Send>(true) { event ->
            if (event.packet is PlayerMoveC2SPacket) {
                timerFlagData?.let { timerFlag ->
                    if (timerFlag.tickTimer.tick(timerFlag.recordTime) || ((world.isAir(player.blockPos) || world.isAir(
                            timerFlag.playerPos
                        )) && timerFlag.flagVL > Burrow.flagVL) || (world.isAir(player.blockPos) && timerCalculator.tick(
                            880
                        ))
                    ) {
                        timerFlagData = null
                        resetTimer()
                        ChatUtil.sendMessage("[Disabler] Removed")
                       return@safeEventListener
                    }
                    if (event.packet != timerFlag.flagPacket) {
                        event.packet.y = timerFlag.playerPos.y.toDouble()
                        event.cancelled = true
                    } else {
                       ChatUtil.sendNoSpamMessage("[Disabler] Bypass Packet Check")
                    }
                    ChatUtil.sendNoSpamMessage("[Disabler] Flagged (VL: ${timerFlag.flagVL})")
                }
            }
        }

        safeEventListener<PacketEvents.Receive>(true) { event ->
            timerFlagData?.let { timerFlag ->
                if (timerFlag.tickTimer.tick(timerFlag.recordTime)) {
                    timerFlagData = null
                    resetTimer()
                    return@safeEventListener
                }
                if (event.packet is PlayerPositionLookS2CPacket) {
                    timerFlag.flagVL += 1
                    ChatUtil.sendMessage("[Disabler] Received")
                }
            }
        }
    }
}