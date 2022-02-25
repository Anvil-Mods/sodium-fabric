package me.jellysquid.mods.sodium.mixin.features.render_layer.leaves;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.sodium.SodiumClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ItemBlockRenderTypes.class)
public class MixinRenderLayers {
    @Mutable
    @Shadow
    @Final
    private static Map<Block, RenderType> BLOCKS;

    @Mutable
    @Shadow
    @Final
    private static Map<Fluid, RenderType> FLUIDS;

    static {
        // Replace the backing collection types with something a bit faster, since this is a hot spot in chunk rendering.
        BLOCKS = new Reference2ReferenceOpenHashMap<>(BLOCKS);
        FLUIDS = new Reference2ReferenceOpenHashMap<>(FLUIDS);
    }
    @Inject(method = "getBlockLayer(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/RenderLayer;", at = @At(value = "RETURN"), cancellable = true)
    private static void redirectLeavesGraphics(BlockState state, CallbackInfoReturnable<RenderType> cir) {
        if (state.getBlock() instanceof LeavesBlock) {
            boolean fancyLeaves = SodiumClientMod.options().quality.leavesQuality.isFancy(Minecraft.getInstance().options.graphicsMode);
            cir.setReturnValue(fancyLeaves ? RenderType.cutoutMipped() : RenderType.solid());
        }
    }
}
