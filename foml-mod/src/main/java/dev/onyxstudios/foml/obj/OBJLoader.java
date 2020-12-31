package dev.onyxstudios.foml.obj;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import dev.onyxstudios.foml.FOML;
import dev.onyxstudios.foml.obj.baked.OBJUnbakedModel;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OBJLoader implements ModelResourceProvider, Function<ResourceManager, ModelResourceProvider> {
    public static final OBJLoader INSTANCE = new OBJLoader();


    private OBJLoader() {
    }

    public OBJUnbakedModel loadModel(Reader reader, String modid, ResourceManager manager, ModelTransformation transform) {
        OBJUnbakedModel model;

        try {
            Obj obj = ObjUtils.convertToRenderable(ObjReader.read(reader));
            model = new OBJUnbakedModel(ObjUtils.triangulate(obj), loadMTL(manager, modid, obj.getMtlFileNames()), transform);
        } catch (IOException e) {
            FOML.LOGGER.error("Could not read obj model!", e);
            return null;
        }

        return model;
    }

    public Map<String, FOMLMaterial> loadMTL(ResourceManager manager, String modid, List<String> mtlNames) throws IOException {
        Map<String, FOMLMaterial> mtls = new LinkedHashMap<>();

        for (String name : mtlNames) {
            Identifier resourceId = new Identifier(modid, "models/" + name);
            // Use 1.0.0 MTL path as a fallback
            if (!manager.containsResource(resourceId)) {
                resourceId = new Identifier(modid, "models/block/" + name);
            }

            // Continue with normal resource loading code
            if(manager.containsResource(resourceId)) {
                Resource resource = manager.getResource(resourceId);

                MtlReader.read(resource.getInputStream()).forEach(mtl -> {
                    mtls.put(mtl.getName(), mtl);
                });
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
        if(identifier.getPath().endsWith(".obj")) {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

            try (Reader reader = new InputStreamReader(resourceManager.getResource(new Identifier(identifier.getNamespace(), "models/" + identifier.getPath())).getInputStream())) {
                return loadModel(reader, identifier.getNamespace(), resourceManager, transform);
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
