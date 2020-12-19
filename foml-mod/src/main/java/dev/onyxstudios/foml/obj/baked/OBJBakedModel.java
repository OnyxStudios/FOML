package dev.onyxstudios.foml.obj.baked;

import dev.onyxstudios.foml.obj.OBJBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class OBJBakedModel implements BakedModel, FabricBakedModel {

    private Mesh mesh;
    private ModelTransformation transformation;
    private final Sprite sprite;

    public OBJBakedModel(OBJBuilder builder, ModelTransformation transformation, Sprite sprite) {
        this.mesh = builder.build();
        this.transformation = transformation;
        this.sprite = sprite;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState blockState, Direction direction, Random random) {
        List<BakedQuad>[] bakedQuads = ModelHelper.toQuadLists(mesh);
        return bakedQuads[direction == null ? 6 : direction.getId()];
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext context) {
        if (mesh != null) {
            context.meshConsumer().accept(mesh);
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        if (mesh != null) {
            context.meshConsumer().accept(mesh);
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getSprite() {
        return this.sprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return transformation;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }
}
