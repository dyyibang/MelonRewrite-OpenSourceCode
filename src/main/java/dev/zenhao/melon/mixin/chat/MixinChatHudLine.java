package dev.zenhao.melon.mixin.chat;

import dev.zenhao.melon.mixins.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = {ChatHudLine.class, ChatHudLine.Visible.class})
public class MixinChatHudLine implements IChatHudLine {
    @Unique
    private int id;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }
}
