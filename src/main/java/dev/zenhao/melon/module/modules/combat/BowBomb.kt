package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.math.RotationUtils
import melon.events.PacketEvents
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.system.util.interfaces.DisplayEnum
import melon.utils.combat.getEntityTarget
import melon.utils.combat.teleport.TeleportUtils.doTeleport
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround
import net.minecraft.util.math.Vec3d


object BowBomb: Module(
    name = "BowBomb",
    langName = "32K弓",
    description = "32k bow",
    category = Category.COMBAT
) {

    private val page = msetting("Page", Page.GENERAL)

    // General
    private val amountProperty by isetting("Amount", 100, 10, 5000, 25).enumIs(page, Page.GENERAL)
    private val autoAim by bsetting("AutoAim", true).enumIs(page, Page.GENERAL)
    private val range by dsetting("Range", 100.0, 10.0, 200.0).enumIs(page, Page.GENERAL).isTrue { autoAim }

    // Teleport
    private val teleport by bsetting("Teleport", false).enumIs(page, Page.TELEPORT)
    private val yOffset by dsetting("YOffset", -0.5, -2.0, 2.0).enumIs(page, Page.TELEPORT)

    init {
        safeEventListener<PacketEvents.Send> {
            if (it.packet is PlayerActionC2SPacket
                && it.packet.action == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM
                && player.inventory.mainHandStack.item == Items.BOW) {

                val target = getEntityTarget(range, mob = false, ani = false) as PlayerEntity?
                val oldPos = player.pos
                if (teleport) {
                    if (target != null) {
                        doTeleport(target.pos.add(Vec3d(0.0, yOffset, 0.0)), false)
                    }
                }
                if (autoAim) doAim(target)
                doBomb(amountProperty)
                if (teleport) {
                    if (target != null) {
                        doTeleport(oldPos, player.isOnGround)
                    }
                }
            }
        }
    }


    private fun SafeClientEvent.doAim(target: PlayerEntity?) {
        val tgt = target ?: return
        RotationManager.startRotation()
        RotationManager.addRotations(tgt.pos, prio = true)
        val rota = RotationUtils.getRotationTo(player.pos, tgt.pos)
        player.networkHandler.sendPacket(LookAndOnGround(rota.x, rota.y, player.isOnGround))
    }

    private fun SafeClientEvent.doBomb(amount: Int) {
        player.networkHandler.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_SPRINTING))
        for (i in 0 until amount) {
            player.networkHandler.sendPacket(PositionAndOnGround(player.x, player.y - 1.0E-9, player.z, true))
            player.networkHandler.sendPacket(PositionAndOnGround(player.x, player.y + 1.0E-9, player.z, false))
        }
    }

    private enum class Page(override val displayName: CharSequence): DisplayEnum {
        GENERAL("General"),
        TELEPORT("Teleport")
    }

}