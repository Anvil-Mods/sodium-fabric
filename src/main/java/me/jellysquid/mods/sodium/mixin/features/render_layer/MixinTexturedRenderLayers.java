package me.jellysquid.mods.sodium.mixin.features.render_layer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.properties.WoodType;

@Mixin(Sheets.class)
public class MixinTexturedRenderLayers {
    @Shadow
    @Final
    public static Map<WoodType, Material> WOOD_TYPE_TEXTURES;

    // Instantiating a SpriteIdentifier every time a sign tries to grab a texture identifier causes a significant
    // performance impact as no RenderLayer will ever be cached for the sprite. Minecraft already maintains a
    // SignType -> SpriteIdentifier cache but for some reason doesn't use it.
    @Inject(method = "getSignTextureId", at = @At("HEAD"), cancellable = true)
    private static void preGetSignTextureId(WoodType type, CallbackInfoReturnable<Material> ci) {
        if (WOOD_TYPE_TEXTURES != null) {
            Material sprite = WOOD_TYPE_TEXTURES.get(type);

            if (type != null) {
                ci.setReturnValue(sprite);
            }
        }
    }
}
