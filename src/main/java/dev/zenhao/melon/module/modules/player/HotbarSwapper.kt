package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import melon.events.TickEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import net.minecraft.screen.slot.SlotActionType

object HotbarSwapper: Module(
    name = "HotbarSwapper",
    langName = "双物品栏",
    category = Category.PLAYER,
    description = "Double hotbar"
) {

    private val inventoryRaw by isetting("InventoryRaw", 3, 1, 3)
    private val start by isetting("Start", 1, 1, 9)
    private val end by isetting("End", 9, 1, 9)

    init {

        safeEventListener<TickEvent.Post> {
            swapStacks()
            disable()
        }

    }

    private fun SafeClientEvent.swapStacks() {
        if (start > end) return

        val raw = inventoryRaw * 9

        for (i in start - 1 until end) {
            if (player.inventory.getStack(i) != player.inventory.getStack(raw + i)) {
                playerController.clickSlot(player.playerScreenHandler.syncId, raw + i, i, SlotActionType.SWAP, player)
            }
        }
    }

}