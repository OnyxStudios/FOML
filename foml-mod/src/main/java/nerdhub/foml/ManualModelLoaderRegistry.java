package nerdhub.foml;

import net.minecraft.client.util.ModelIdentifier;

import java.util.HashSet;
import java.util.Set;

/**
 * This must only be used for models that need to be loaded for other uses than blocks or items.
 */
public class ManualModelLoaderRegistry {
    public static final ManualModelLoaderRegistry INSTANCE = new ManualModelLoaderRegistry();

    private final Set<ModelIdentifier> models = new HashSet<>();

    private ManualModelLoaderRegistry() {
    }

    public void register(ModelIdentifier identifier) {
        this.models.add(identifier);
    }

    public Set<ModelIdentifier> getModels() {
        return this.models;
    }
}
