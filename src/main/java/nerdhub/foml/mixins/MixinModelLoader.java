package nerdhub.foml.mixins;

import nerdhub.foml.obj.OBJLoader;
import nerdhub.foml.obj.baked.OBJUnbakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStreamReader;
import java.io.Reader;

@Mixin(ModelLoader.class)
public abstract class MixinModelLoader {

    @Final
    @Shadow
    private ResourceManager resourceManager;

    @Inject(method = "loadModel", at = @At(target = "Lnet/minecraft/client/render/model/ModelLoader;putModel(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/UnbakedModel;)V", value = "INVOKE", shift = At.Shift.BEFORE), cancellable = true)
    private void loadModel(Identifier identifier, CallbackInfo ci) throws Exception {
        if(OBJLoader.INSTANCE.isRegistered(identifier.getNamespace()) && identifier.getPath().endsWith(".obj")) {
            Resource resource = this.resourceManager.getResource(new Identifier(identifier.getNamespace(), "models/" + identifier.getPath()));

            try (Reader reader = new InputStreamReader(resource.getInputStream())) {
                this.putModel(identifier, new OBJUnbakedModel(OBJLoader.INSTANCE.loadModel(reader, identifier.getNamespace(), this.resourceManager)));
            }

            ci.cancel();
        }
    }

    @Shadow
    abstract void putModel(Identifier identifier, UnbakedModel unbakedModel);
}
