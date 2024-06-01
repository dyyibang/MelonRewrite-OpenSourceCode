package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import net.minecraft.item.ItemStack

object ToolTips : Module(name = "ToolTips", langName = "工具提示", category = Category.RENDER) {
    var middleClickOpen = bsetting("MiddleClickOpen", true)
    var storage = bsetting("Storage", true)
    var maps = bsetting("Maps", true)
    var shulkerRegear = bsetting("ShulkerRegear", true)

    fun hasItems(itemStack: ItemStack): Boolean {
        val compoundTag = itemStack.getSubNbt("BlockEntityTag")
        return compoundTag != null && compoundTag.contains("Items", 9)
    }
}