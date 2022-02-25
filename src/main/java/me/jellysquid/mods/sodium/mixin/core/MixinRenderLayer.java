package me.jellysquid.mods.sodium.mixin.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import me.jellysquid.mods.sodium.interop.vanilla.mixin.MultiPhaseParametersExtended;
import me.jellysquid.mods.sodium.interop.vanilla.mixin.RenderPhaseShaderAccess;
import me.jellysquid.mods.sodium.interop.vanilla.mixin.ShaderExtended;
import me.jellysquid.mods.sodium.interop.vanilla.mixin.ShaderTexture;
import me.jellysquid.mods.sodium.interop.vanilla.mixin.ShaderTextureProvider;
import me.jellysquid.mods.sodium.opengl.array.VertexArrayDescription;
import me.jellysquid.mods.sodium.opengl.array.VertexArrayResourceBinding;
import me.jellysquid.mods.sodium.opengl.attribute.VertexAttribute;
import me.jellysquid.mods.sodium.opengl.attribute.VertexAttributeBinding;
import me.jellysquid.mods.sodium.opengl.device.RenderDevice;
import me.jellysquid.mods.sodium.opengl.pipeline.Pipeline;
import me.jellysquid.mods.sodium.opengl.shader.Program;
import me.jellysquid.mods.sodium.opengl.types.BlendFunction;
import me.jellysquid.mods.sodium.opengl.types.CullingMode;
import me.jellysquid.mods.sodium.opengl.types.DepthFunc;
import me.jellysquid.mods.sodium.opengl.types.RenderState;
import me.jellysquid.mods.sodium.opengl.types.WriteMask;
import me.jellysquid.mods.sodium.render.immediate.RenderImmediate;
import me.jellysquid.mods.sodium.render.immediate.VanillaShaderInterface;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

@Mixin(RenderType.class)
public class MixinRenderLayer {
    @Mixin(targets = "net/minecraft/client/render/RenderLayer$MultiPhaseParameters")
    public static class MixinMultiPhaseParameters implements MultiPhaseParametersExtended {
        @Shadow
        @Final
        private RenderStateShard.EmptyTextureStateShard texture;

        @Shadow
        @Final
        private RenderStateShard.ShaderStateShard shader;

        @Shadow
        @Final
        private RenderStateShard.TransparencyStateShard transparency;

        @Shadow
        @Final
        private RenderStateShard.DepthTestStateShard depthTest;

        @Shadow
        @Final
        private RenderStateShard.CullStateShard cull;

        @Shadow
        @Final
        private RenderStateShard.LightmapStateShard lightmap;

        @Shadow
        @Final
        private RenderStateShard.OverlayStateShard overlay;

        @Shadow
        @Final
        private RenderStateShard.LayeringStateShard layering; // TODO: implement

        @Shadow
        @Final
        private RenderStateShard.OutputStateShard target; // TODO: implement

        @Shadow
        @Final
        private RenderStateShard.TexturingStateShard texturing; // TODO: implement

        @Shadow
        @Final
        private RenderStateShard.WriteMaskStateShard writeMaskState;

        @Shadow
        @Final
        private RenderStateShard.LineStateShard lineWidth; // TODO: implement

        @Override
        public RenderState createRenderState() {
            return RenderState.builder()
                    .setBlendFunction(createBlendFunction(this.transparency))
                    .setDepthFunc(createDepthFunction(this.depthTest))
                    .setCullingMode(createCullingMode(this.cull))
                    .setWriteMask(createWriteMask(this.writeMaskState))
                    .build();
        }

        @Override
        public Program<VanillaShaderInterface> createProgram() {
            if (this.shader instanceof RenderPhaseShaderAccess access) {
                var supplier = access.getShader();
                var shader = supplier.get();

                Validate.notNull(shader, "Shader program not available");

                if (shader instanceof ShaderExtended extended) {
                    return extended.sodium$getShader();
                } else {
                    throw new UnsupportedOperationException("Shader doesn't implement required interface");
                }
            }

            throw new UnsupportedOperationException("Unsupported shader value");
        }

        @Override
        public ShaderTexture[] createShaderTextures() {
            var textures = new ShaderTexture[12];

            if (this.overlay instanceof ShaderTextureProvider provider) {
                populateTextures(textures, provider.getTextures());
            }

            if (this.lightmap instanceof ShaderTextureProvider provider) {
                populateTextures(textures, provider.getTextures());
            }

            if (this.texture instanceof ShaderTextureProvider provider) {
                populateTextures(textures, provider.getTextures());
            }

            return textures;
        }

        @Override
        public Vector4f getDefaultColorModulator() {
            if (this.lightmap == RenderStateShard.LIGHTMAP) {
                return new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
            }

            return null;
        }

        private static void populateTextures(ShaderTexture[] outputs, List<ShaderTexture> inputs) {
            for (var input : inputs) {
                outputs[input.target()] = input;
            }
        }

        private static WriteMask createWriteMask(RenderStateShard.WriteMaskStateShard value) {
            return new WriteMask(value.writeColor, value.writeDepth);
        }

        private static CullingMode createCullingMode(RenderStateShard.CullStateShard value) {
            if (value == RenderStateShard.CULL) {
                return CullingMode.ENABLE;
            } else if (value == RenderStateShard.NO_CULL) {
                return CullingMode.DISABLE;
            }

            throw new UnsupportedOperationException();
        }

        private static DepthFunc createDepthFunction(RenderStateShard.DepthTestStateShard value) {
            if (value == RenderStateShard.NO_DEPTH_TEST) {
                return DepthFunc.ALWAYS;
            } else if (value == RenderStateShard.EQUAL_DEPTH_TEST) {
                return DepthFunc.EQUAL;
            } else if (value == RenderStateShard.LEQUAL_DEPTH_TEST) {
                return DepthFunc.LESS_THAN_OR_EQUAL;
            }

            throw new UnsupportedOperationException();
        }

        private static BlendFunction createBlendFunction(RenderStateShard.TransparencyStateShard value) {
            if (value == RenderStateShard.NO_TRANSPARENCY) {
                return null;
            } else if (value == RenderStateShard.ADDITIVE_TRANSPARENCY) {
                return BlendFunction.of(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            } else if (value == RenderStateShard.LIGHTNING_TRANSPARENCY) {
                return BlendFunction.of(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            } else if (value == RenderStateShard.GLINT_TRANSPARENCY) {
                return BlendFunction.of(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            } else if (value == RenderStateShard.CRUMBLING_TRANSPARENCY) {
                return BlendFunction.of(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            } else if (value == RenderStateShard.TRANSLUCENT_TRANSPARENCY) {
                return BlendFunction.of(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }

            throw new UnsupportedOperationException();
        }
    }

    @Mixin(targets = "net/minecraft/client/render/RenderLayer$MultiPhase")
    public static class MixinMultiPhase extends RenderType {
        @Shadow
        @Final
        private CompositeState phases;

        private Pipeline<VanillaShaderInterface, VanillaShaderInterface.BufferTarget> pipeline;
        private ShaderTexture[] textures;

        private Vector4f defaultColorModulator;

        public MixinMultiPhase(String name, VertexFormat vertexFormat, VertexFormat.Mode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
            super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        }

        @Override
        public void end(BufferBuilder buffer, int cameraX, int cameraY, int cameraZ) {
            if (this.pipeline != null && !this.pipeline.getProgram().isHandleValid()) {
                this.destroy();
            }

            if (this.pipeline == null) {
                this.init();
            }

            if (buffer.building()) {
                if (this.pipeline.getState().blendFunction != null) {
                    buffer.setQuadSortOrigin((float) cameraX, (float) cameraY, (float) cameraZ);
                }

                buffer.end();

                var data = buffer.popNextBuffer();
                var params = data.getFirst();

                // VANILLA BUG: LightmapTextureManager resets the color back to all-white when using the vanilla lightmap
                // render phase, but we don't use that code path. Without this, many things will fail to render correctly.
                if (this.defaultColorModulator != null) {
                    RenderSystem.setShaderColor(this.defaultColorModulator.x, this.defaultColorModulator.y, this.defaultColorModulator.z,
                            this.defaultColorModulator.w);
                }

                RenderImmediate.getInstance()
                        .draw(this.pipeline, this.textures, data.getSecond(), params.mode(), params.format(), params.vertexCount(), params.indexType(), params.indexCount(), params.sequentialIndex());
            }
        }

        private void destroy() {
            RenderDevice.INSTANCE.deletePipeline(this.pipeline);
            this.pipeline = null;
            this.textures = null;
        }

        private void init() {
            if ((Object) this.phases instanceof MultiPhaseParametersExtended params) {
                this.pipeline = this.createPipeline(params.createRenderState(), params.createProgram());
                this.textures = params.createShaderTextures();
                this.defaultColorModulator = params.getDefaultColorModulator();
            } else {
                throw new UnsupportedOperationException("Not able to convert multi-phase parameters into pipeline");
            }
        }

        private Pipeline<VanillaShaderInterface, VanillaShaderInterface.BufferTarget> createPipeline(RenderState renderState, Program<VanillaShaderInterface> program) {
            var vertexArray = new VertexArrayDescription<>(VanillaShaderInterface.BufferTarget.values(),
                    List.of(new VertexArrayResourceBinding<>(VanillaShaderInterface.BufferTarget.VERTICES, createVanillaVertexBindings(this.format()))));

            return RenderDevice.INSTANCE.createPipeline(renderState, program, vertexArray);
        }

        private static VertexAttributeBinding[] createVanillaVertexBindings(VertexFormat vertexFormat) {
            var elements = vertexFormat.getElements();
            var bindings = new ArrayList<VertexAttributeBinding>();

            for (int i = 0; i < elements.size(); i++) {
                VertexFormatElement element = elements.get(i);

                if (element.getUsage() == VertexFormatElement.Usage.PADDING) {
                    continue;
                }

                var format = element.getType().getGlType();
                var count = element.getCount();
                var size = element.getByteSize();
                var normalized = isVanillaAttributeNormalized(element.getUsage());
                var intType = isVanillaIntType(element.getUsage(), element.getType());

                var attribute = new VertexAttribute(format, size, count, normalized, vertexFormat.offsets.getInt(i), intType);

                bindings.add(new VertexAttributeBinding(i, attribute));
            }

            return bindings.toArray(VertexAttributeBinding[]::new);
        }

        private static boolean isVanillaIntType(VertexFormatElement.Usage type, VertexFormatElement.Type dataType) {
            return type == VertexFormatElement.Usage.UV && dataType != VertexFormatElement.Type.FLOAT;
        }

        private static boolean isVanillaAttributeNormalized(VertexFormatElement.Usage type) {
            return type == VertexFormatElement.Usage.NORMAL || type == VertexFormatElement.Usage.COLOR;
        }
    }
}
