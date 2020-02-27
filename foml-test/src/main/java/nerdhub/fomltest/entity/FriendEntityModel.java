package nerdhub.fomltest.entity;

import nerdhub.fomltest.FOMLTest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class FriendEntityModel<T extends FriendEntity> extends EntityModel<T> {
    public static final ModelIdentifier MODEL = new ModelIdentifier(new Identifier(FOMLTest.MODID, "entity/friend.obj"), null);

    public FriendEntityModel() {
        super(texture -> RenderLayer.getSolid());
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        MatrixStack.Entry entry = matrices.peek();
        BakedModel model = MinecraftClient.getInstance().getBakedModelManager().getModel(MODEL);
        for (BakedQuad quad : model.getQuads(null, null, null)) {
            vertexConsumer.quad(entry, quad, red, green, blue, light, overlay);
        }
    }
}
