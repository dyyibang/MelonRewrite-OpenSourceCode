package dev.zenhao.melon.mixin.font;

import dev.zenhao.melon.Melon;
import dev.zenhao.melon.mixins.ITextRendererDrawer;
import dev.zenhao.melon.module.modules.client.OverrideFont;
import dev.zenhao.melon.utils.extension.MathKt;
import melon.system.render.newfont.FontRenderers;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(TextRenderer.class)
public class MixinTextRenderer {
    @Final
    @Shadow
    boolean validateAdvance;
    @Mutable
    @Final
    @Shadow
    private Function<Identifier, FontStorage> fontStorageAccessor;

    public MixinTextRenderer(Function<Identifier, FontStorage> fontStorageAccessor) {
        this.fontStorageAccessor = fontStorageAccessor;
    }

    @Inject(method = "drawInternal(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I", at = @At(value = "HEAD"), cancellable = true)
    public void drawLayer(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColor, int light, boolean mirror, CallbackInfoReturnable<Integer> cir) {
        if (Melon.Companion.isReady() && OverrideFont.INSTANCE.isEnabled()) {
            TextRenderer.Drawer drawer = new TextRenderer(fontStorageAccessor, validateAdvance).new Drawer(vertexConsumers, x, y, color, shadow, matrix, layerType, light);
            ITextRendererDrawer converter = ((ITextRendererDrawer) drawer);
            converter.setX(x);
            converter.setY(y);
            FontRenderers.cn.drawString(new MatrixStack(), text, x + FontRenderers.cn.getStringWidth(text), y - 1f, color, shadow);
            cir.setReturnValue(MathKt.fastCeil(x + FontRenderers.cn.getStringWidth(text)));
        }
    }

    @Inject(method = "drawInternal(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", at = @At(value = "HEAD"), cancellable = true)
    public void drawLayer(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextRenderer.TextLayerType layerType, int backgroundColor, int light, CallbackInfoReturnable<Integer> cir) {
        if (Melon.Companion.isReady() && OverrideFont.INSTANCE.isEnabled()) {
            TextRenderer.Drawer drawer = new TextRenderer(fontStorageAccessor, validateAdvance).new Drawer(vertexConsumerProvider, x, y, color, shadow, matrix, layerType, light);
            ITextRendererDrawer converter = ((ITextRendererDrawer) drawer);
            converter.setX(x);
            converter.setY(y);
            StringBuilder sb = new StringBuilder();
            text.accept((index, style, codePoint) -> {
                sb.appendCodePoint(codePoint);
                return true;
            });
            String result = sb.toString();
            FontRenderers.cn.drawString(new MatrixStack(), result, x + FontRenderers.cn.getStringWidth(result), y - 1f, color, shadow);
            cir.setReturnValue(MathKt.fastCeil(x + FontRenderers.cn.getStringWidth(result)));
        }
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/OrderedText;)I", at = @At("HEAD"), cancellable = true)
    public void getCustomWidth(OrderedText text, CallbackInfoReturnable<Integer> cir) {
        if (Melon.Companion.isReady() && OverrideFont.INSTANCE.isEnabled()) {
            cir.setReturnValue((int) FontRenderers.cn.getStringWidth(text.toString()));
        }
    }

    @Inject(method = "getWidth(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    public void getCustomWidth(String text, CallbackInfoReturnable<Integer> cir) {
        if (Melon.Companion.isReady() && OverrideFont.INSTANCE.isEnabled()) {
            cir.setReturnValue((int) FontRenderers.cn.getStringWidth(text));
        }
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/StringVisitable;)I", at = @At("HEAD"), cancellable = true)
    public void getCustomWidth(StringVisitable text, CallbackInfoReturnable<Integer> cir) {
        if (Melon.Companion.isReady() && OverrideFont.INSTANCE.isEnabled()) {
            cir.setReturnValue((int) FontRenderers.cn.getStringWidth(text.toString()));
        }
    }
}
