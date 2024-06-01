package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.HotbarManager.spoofHotbarBypass
import dev.zenhao.melon.manager.HotbarManager.spoofInvBypass
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.inventory.HotbarSlot
import melon.events.input.MouseClickEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.system.util.interfaces.DisplayEnum
import melon.utils.chat.ChatUtil
import melon.utils.inventory.slot.allSlots
import melon.utils.inventory.slot.firstItem
import melon.utils.inventory.slot.hotbarSlots
import melon.utils.screen.ScreenUtils.safeReturn
import net.minecraft.item.Items
import net.minecraft.util.Hand

object MCP : Module(
    name = "MCP", langName = "中键扔珍珠", description = "Middle pearl", category = Category.MISC
) {
    private val silentMode by msetting("SilentMode", SilentMode.Spoof)

    init {
        safeEventListener<MouseClickEvent> {
            if (mc.currentScreen != null) return@safeEventListener

            if (it.button == MouseClickEvent.MouseButton.MIDDLE && it.action == MouseClickEvent.MouseAction.PRESS) {
                if (mc.currentScreen.safeReturn()) return@safeEventListener
                if (mc.targetedEntity == null) {
                    val slot =
                        player.hotbarSlots.firstItem(Items.ENDER_PEARL)
                            ?: (if (silentMode != SilentMode.Spoof) player.allSlots.firstItem(Items.ENDER_PEARL)
                            else player.hotbarSlots.firstItem(Items.ENDER_PEARL))?.let { item -> HotbarSlot(item) }
                            ?: return@safeEventListener

                    when (silentMode) {
                        SilentMode.Spoof -> {
                            spoofHotbar(slot) {
                                interactPearl()
                            }
                        }
                        SilentMode.SpoofBypass -> {
                            spoofHotbarBypass(slot) {
                                interactPearl()
                            }
                        }
                        SilentMode.InvPick -> {
                            spoofInvBypass(slot) {
                                interactPearl()
                            }
                        }
                    }

                    spoofHotbarBypass(slot) {
                        RotationManager.stopRotation()
                        playerController.interactItem(player, Hand.MAIN_HAND)
                        RotationManager.startRotation()
                    }
                } else {
                    ChatUtil.sendErrorMessage("Pearls will be thrown on mobs!!! Cancel throwing!!!")
                }
            }
        }
    }

    private fun SafeClientEvent.interactPearl() {
        RotationManager.stopRotation()
        playerController.interactItem(player, Hand.MAIN_HAND)
        RotationManager.startRotation()
    }

    private enum class SilentMode(override val displayName: CharSequence): DisplayEnum {
        Spoof("Spoof"),
        SpoofBypass("SpoofBypass"),
        InvPick("InvPick")
    }
}