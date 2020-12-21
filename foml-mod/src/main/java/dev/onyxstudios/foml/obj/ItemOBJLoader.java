package dev.onyxstudios.foml.obj;

import com.google.gson.*;
import dev.onyxstudios.foml.FOML;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Function;

/***
 *  ItemOBJLoader
 *  Child class of the OBJ loader that loads basic item OBJ models with JSON model transformations.
 *
 *  Created by jard at 2:27 AM on September 22, 2019.
 ***/
public class ItemOBJLoader implements ModelVariantProvider, Function<ResourceManager, ModelVariantProvider> {
    public static ItemOBJLoader INSTANCE = new ItemOBJLoader();
    public static final Gson GSON = (new GsonBuilder())
            .registerTypeAdapter (ModelTransformation.class, new ItemOBJLoader.ModelTransformDeserializer())
            .registerTypeAdapter (Transformation.class, new ItemOBJLoader.TransformDeserializer())
            .create ();
    private static final OBJLoader OBJ_LOADER = OBJLoader.INSTANCE;

    @Override
    public UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) {
        if(modelId.getVariant().equals("inventory")) {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

            Identifier modelPath = new Identifier (modelId.getNamespace (),
                    "models/item/" + modelId.getPath () + ".json");

            if (resourceManager.containsResource(modelPath)) {
                try (Reader reader = new InputStreamReader(resourceManager.getResource(modelPath).getInputStream())) {
                    JsonObject rawModel = JsonHelper.deserialize(reader);

                    JsonElement parent = rawModel.get("parent");
                    if (parent instanceof JsonPrimitive && ((JsonPrimitive) parent).isString()) {
                        String objPath = parent.getAsString();
                        if (!objPath.endsWith(".obj"))
                            throw new IllegalStateException("Parent of JsonOBJ model must be a .obj file.");

                        Identifier parentPath = new Identifier(objPath);

                        ModelTransformation transformation = null;
                        if (rawModel.has("display")) {
                            JsonObject rawTransform = JsonHelper.getObject(rawModel, "display");
                            transformation = GSON.fromJson(rawTransform, ModelTransformation.class);
                        }

                        return OBJ_LOADER.loadModelResource(parentPath,
                                context, transformation);
                    }
                } catch (Exception e) {
                    // Silently ignore general IllegalStateExceptions, as all vanilla models in a registered namespace would
                    // otherwise spew the console with this error.
                    if (!(e instanceof IllegalStateException)) {
                        FOML.LOGGER.error("Unable to load OBJ Model, Source: " + modelId.toString(), e);
                    }
                }
            }
        }
        return null;
    }

    @Environment(EnvType.CLIENT)
    public static class ModelTransformDeserializer extends ModelTransformation.Deserializer {
        protected ModelTransformDeserializer () {
            super ();
        }
    }
    @Environment(EnvType.CLIENT)
    public static class TransformDeserializer extends Transformation.Deserializer {
        protected TransformDeserializer () {
            super ();
        }
    }

    @Override
    public ModelVariantProvider apply(ResourceManager manager) {
        return this;
    }
}
