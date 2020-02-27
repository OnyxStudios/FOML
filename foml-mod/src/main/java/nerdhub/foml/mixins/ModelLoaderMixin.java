package nerdhub.foml.mixins;

import nerdhub.foml.ManualModelLoaderRegistry;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {

    @Shadow
    protected abstract void addModel(ModelIdentifier modelId);

    @Shadow
    @Final
    public static ModelIdentifier MISSING;

    /**
     * This method is only called in the constructor of ModelLoader, this allows to inject models earlier than on the
     * constructor return.
     */
    @Inject(method = "addModel", at = @At("RETURN"))
    public void injectModels(ModelIdentifier modelId, CallbackInfo ci) {
        // Only inject models once
        if (modelId == MISSING) {
            for (ModelIdentifier id : ManualModelLoaderRegistry.INSTANCE.getModels()) {
                this.addModel(id);
            }
        }
    }
}
