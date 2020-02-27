package nerdhub.fomltest.entity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class FriendEntityRenderer<T extends FriendEntity, M extends FriendEntityModel<T>> extends MobEntityRenderer<T, M> {
    public FriendEntityRenderer(EntityRenderDispatcher dispatcher, M model) {
        super(dispatcher, model, 1);
    }

    @Override
    public void render(T mobEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int overlay) {
        matrixStack.push();
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(mobEntity, matrixStack, tickDelta);
        matrixStack.translate(0.0D, -1.5010000467300415D, 0.0D);
        super.render(mobEntity, yaw, tickDelta, matrixStack, vertexConsumerProvider, overlay);
        matrixStack.pop();
    }

    @Override
    public Identifier getTexture(T entity) {
        return null;
    }
}
