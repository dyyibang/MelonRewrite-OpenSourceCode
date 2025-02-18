package dev.zenhao.melon.manager

import dev.zenhao.melon.utils.inventory.*
import melon.events.PacketEvents
import melon.events.player.PlayerMotionEvent
import melon.system.event.AlwaysListening
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.utils.TickTimer
import melon.utils.inventory.InvUtils.hotbarIsFull
import melon.utils.inventory.slot.allSlots
import melon.utils.inventory.slot.hotbarSlots
import melon.utils.inventory.slot.offhandSlot
import melon.utils.player.updateController
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.screen.slot.Slot

@Suppress("NOTHING_TO_INLINE")
object HotbarManager : AlwaysListening {
    var serverSideHotbar = 0; private set
    var swapTime = 0L; private set

    private val tick = TickTimer()

    val ClientPlayerEntity.serverSideItem: ItemStack
        get() = inventory.main[serverSideHotbar]

    fun onInit() {
        safeEventListener<PacketEvents.Send>(Int.MIN_VALUE) {
            if (it.cancelled) return@safeEventListener

            when (it.packet) {
                is UpdateSelectedSlotC2SPacket -> {
                    synchronized(HotbarManager) {
                        serverSideHotbar = it.packet.selectedSlot
                        swapTime = System.currentTimeMillis()
                    }
                }
            }
        }

        safeEventListener<PlayerMotionEvent>(Int.MAX_VALUE) {
            if (tick.tickAndReset(5000)) {
                playerController.updateController()
            }
        }
    }

    inline fun SafeClientEvent.spoofHotbarBypass(slot: HotbarSlot, crossinline block: () -> Unit) {
        synchronized(HotbarManager) {
            val swap = slot.hotbarSlot != serverSideHotbar
            if (swap) {
                inventoryTaskNow {
                    val hotbarSlot = player.hotbarSlots[serverSideHotbar]
                    swapWith(slot, hotbarSlot)
                    action { block.invoke() }
                    swapWith(slot, hotbarSlot)
                }
            } else {
                block.invoke()
            }
        }
    }

    inline fun SafeClientEvent.spoofInvBypass(slot: HotbarSlot, swap: Boolean = false, crossinline block: () -> Unit) {
        if (slot.hotbarSlot in 0 until 9) {
            if (swap) spoofHotbarBypass(slot, block)
            else spoofHotbar(slot, block)
            return
        }
        if (!hotbarIsFull()) return

        playerController.pickFromInventory(slot.allSlot)
        block.invoke()
        playerController.pickFromInventory(slot.allSlot)
    }

    inline fun SafeClientEvent.spoofInventory(slot: HotbarSlot, crossinline block: () -> Unit) {
        synchronized(HotbarManager) {
            inventoryTaskNow {
                pickUp(slot)
                action { block.invoke() }
                pickUp(slot)
            }
        }
    }

    inline fun SafeClientEvent.spoofOffhand(slot: Slot, crossinline block: () -> Unit) {
        synchronized(HotbarManager) {
            inventoryTaskNow {
                val offhand = player.offhandSlot
                pickUp(slot)
                pickUp(offhand)
                action { block.invoke() }
                pickUp(slot)
                pickUp(offhand)
            }
        }
    }

    inline fun SafeClientEvent.moveInv(slot: Int, crossinline block: () -> Unit) {
        synchronized(HotbarManager) {
            inventoryTaskNow {
                quickMove(player.allSlots[slot])
                block.invoke()
            }
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: HotbarSlot) {
        return spoofHotbar(slot.hotbarSlot)
    }

    inline fun SafeClientEvent.spoofHotbar(slot: Int) {
        if (serverSideHotbar != slot && slot >= 0) {
            connection.sendPacket(UpdateSelectedSlotC2SPacket(slot))
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: HotbarSlot, crossinline block: () -> Unit) {
        synchronized(HotbarManager) {
            spoofHotbar(slot)
            block.invoke()
            resetHotbar()
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: Int, crossinline block: () -> Unit) {
        synchronized(HotbarManager) {
            spoofHotbar(slot)
            block.invoke()
            resetHotbar()
        }
    }

    inline fun SafeClientEvent.resetHotbar() {
        synchronized(HotbarManager) {
            val slot = playerController.lastSelectedSlot
            if (serverSideHotbar != slot) {
                spoofHotbar(slot)
            }
        }
    }

    inline fun SafeClientEvent.bypassTo(slot: HotbarSlot) {
        synchronized(HotbarManager) {
            inventoryTaskNow {
                swapWith(slot, player.hotbarSlots[player.inventory.selectedSlot])
            }
        }
    }
}
