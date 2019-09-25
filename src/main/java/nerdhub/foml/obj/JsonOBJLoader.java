package nerdhub.foml.obj;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import nerdhub.foml.FOML;
import nerdhub.foml.obj.baked.OBJUnbakedModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/***
 *  JsonOBJLoader
 *  Child class of the OBJ loader that loads basic item OBJ models with JSON model transformations.
 *
 *  Created by jard at 2:27 AM on September 22, 2019.
 ***/
public class JsonOBJLoader extends OBJLoader {
    public static final JsonOBJLoader INSTANCE = new JsonOBJLoader();
    public static final Gson GSON = (new GsonBuilder())
            .registerTypeAdapter (ModelTransformation.class, new ModelTransformDeserializer ())
            .registerTypeAdapter (Transformation.class, new TransformDeserializer ())
            .create ();

    @Override
    public UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) {
        if(OBJLoader.INSTANCE.isRegistered(identifier.getNamespace())) {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

            Identifier modelPath = new Identifier (identifier.getNamespace (),
                    "models/" + identifier.getPath () + ".obj-json");

            try (Reader reader = new InputStreamReader(resourceManager.getResource(modelPath).getInputStream())) {
                JsonObject rawModel = JsonHelper.deserialize (reader);

                String objPath = rawModel.get ("parent").getAsString ();
                if (! objPath.endsWith (".obj"))
                    throw new IOException ("Parent of JsonOBJ model must be a .obj file.");

                Identifier parentPath = new Identifier (objPath);

                ModelTransformation transformation = null;
                if (rawModel.has ("display")) {
                    JsonObject rawTransform = JsonHelper.getObject (rawModel, "display");
                    transformation = GSON.fromJson (rawTransform, ModelTransformation.class);
                }

                return (OBJUnbakedModel) OBJLoader.INSTANCE.loadModelResource (parentPath,
                        modelProviderContext, transformation);
            } catch (IOException e) {
                // Silently ignore filenotfoundexceptions, as all models in a registered namespace would otherwise
                // spew the console with errors
                if (! (e instanceof FileNotFoundException)) {
                    FOML.LOGGER.error("Unable to load OBJ Model, Source: " + identifier.toString(), e);
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
}
