package nerdhub.foml.obj;

import de.javagl.obj.*;
import nerdhub.foml.FOML;
import nerdhub.foml.obj.baked.OBJUnbakedModel;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class OBJLoader implements ModelResourceProvider, Function<ResourceManager, ModelResourceProvider> {
    public static final OBJLoader INSTANCE = new OBJLoader();
    private Set<String> objHandlers = new HashSet<>();

    public void registerDomain(String modid) {
        if(!objHandlers.contains(modid)) {
            objHandlers.add(modid);
        }else {
            FOML.LOGGER.warn("Duplicate registry of OBJ Handler, Source: " + modid);
        }
    }

    public boolean isRegistered(String modid) {
        return objHandlers.contains(modid);
    }

    public OBJBuilder loadModel(Reader reader, String modid, ResourceManager manager) {
        OBJBuilder model;
        try {
            Obj obj = ObjUtils.convertToRenderable(ObjReader.read(reader));
            model = new OBJBuilder(ObjUtils.triangulate(obj), loadMTL(manager, modid, obj.getMtlFileNames()));
        } catch (IOException e) {
            FOML.LOGGER.error("Could not read obj model!", e);
            return null;
        }

        return model;
    }

    public List<Mtl> loadMTL(ResourceManager manager, String modid, List<String> mtlNames) throws IOException {
        List<Mtl> mtls = new ArrayList<>();

        for (String name : mtlNames) {
            Identifier resourceId = new Identifier(modid, "models/" + name);
            // Use 1.0.0 MTL path as a fallback
            if (!manager.containsResource(resourceId)) {
                resourceId = new Identifier(modid, "models/block/" + name);
            }

            // Continue with normal resource loading code
            if(manager.containsResource(resourceId)) {
                Resource resource = manager.getResource(resourceId);
                mtls.addAll(MtlReader.read(resource.getInputStream()));
            } else {
                FOML.LOGGER.warn("Warning, a model specifies an MTL File but it could not be found! Source: " + modid + ":" + name);
            }
        }

        return mtls;
    }

    @Override
    public UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) {
        return loadModelResource (identifier, modelProviderContext, ModelTransformation.NONE);
    }

    protected UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext,
                                          ModelTransformation transform) {
        if(isRegistered(identifier.getNamespace()) && identifier.getPath().endsWith(".obj")) {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

            try (Reader reader = new InputStreamReader(resourceManager.getResource(new Identifier(identifier.getNamespace(), "models/" + identifier.getPath())).getInputStream())) {
                OBJBuilder model = loadModel(reader, identifier.getNamespace(), resourceManager);
                return new OBJUnbakedModel(model, transform);
            } catch (IOException e) {
                FOML.LOGGER.error("Unable to load OBJ Model, Source: " + identifier.toString(), e);
            }
        }

        return null;
    }

    @Override
    public ModelResourceProvider apply(ResourceManager manager) {
        return this;
    }
}
