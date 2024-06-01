package melon.utils.screen

import dev.zenhao.melon.gui.clickgui.new.MelonClickGui
import dev.zenhao.melon.gui.clickgui.new.MelonHudEditor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.*
import net.minecraft.item.ItemGroups

object ScreenUtils {
    val Screen?.isMelonUIScreen: Boolean
        get() = this is MelonClickGui || this is MelonHudEditor

    fun Screen.notWhiteListScreen(): Boolean {
        return this is CreativeInventoryScreen && CreativeInventoryScreen.selectedTab === ItemGroups.getSearchGroup()
                || this is ChatScreen
                || this is SignEditScreen
                || this is AnvilScreen
                || this is AbstractCommandBlockScreen
                || this is StructureBlockScreen
    }

    fun Screen?.safeReturn(): Boolean {
        return this != null && this.notWhiteListScreen() || MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null || this.isMelonUIScreen
    }
}