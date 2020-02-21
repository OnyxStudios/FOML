package nerdhub.foml.mixins;

import nerdhub.foml.ManualModelLoaderRegistry;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {

    @Shadow protected abstract void addModel(ModelIdentifier modelId);

    @Inject(method = "<init>", at= @At(value = "RETURN"))
    public void init(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int i, CallbackInfo ci) {
        profiler.push("manual_obj_models");
        for (ModelIdentifier id : ManualModelLoaderRegistry.INSTANCE.getModels()) {
            this.addModel(id);
        }
        profiler.pop();
    }

}
