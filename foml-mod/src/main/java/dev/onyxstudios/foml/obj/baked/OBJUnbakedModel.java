package dev.onyxstudios.foml.obj.baked;

import com.mojang.datafixers.util.Pair;
import de.javagl.obj.FloatTuple;
import de.javagl.obj.Mtl;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjSplitting;
import dev.onyxstudios.foml.obj.FOMLMaterial;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.*;
import java.util.function.Function;

public class OBJUnbakedModel implements UnbakedModel {

    public static final SpriteIdentifier DEFAULT_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, null);

    private final Obj obj;
    private final Map<String, FOMLMaterial> mtls;
    private final ModelTransformation transform;
    private final SpriteIdentifier sprite;

    public OBJUnbakedModel(Obj obj, Map<String, FOMLMaterial> mtls, ModelTransformation transform) {
        this.obj = obj;
        this.mtls = mtls;
        this.transform = transform == null ? ModelTransformation.NONE : transform;

        Mtl mtl = this.findMtlForName("sprite");
        this.sprite = mtls.size() > 0
                ? new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier((mtl == null ? mtls.values().iterator().next() : mtl).getMapKd()))
                : DEFAULT_SPRITE;
    }

    private FOMLMaterial findMtlForName(String name) {
        return mtls.get(name);
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptySet();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        List<SpriteIdentifier> sprites = new ArrayList<>();
        mtls.values().forEach(mtl -> sprites.add(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(mtl.getMapKd()))));

        return sprites;
    }

    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings bakeSettings, Identifier modelId) {
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        Mesh mesh = null;

        if (renderer != null) {
            Map<String, Obj> materialGroups = ObjSplitting.splitByMaterialGroups(obj);
            MeshBuilder builder = renderer.meshBuilder();
            QuadEmitter emitter = builder.getEmitter();

            for (Map.Entry<String, Obj> entry : materialGroups.entrySet()) {
                String matName = entry.getKey();
                Obj matGroupObj = entry.getValue();

                FOMLMaterial mtl = findMtlForName(matName);
                int color = -1;

                Sprite mtlSprite = textureGetter.apply(DEFAULT_SPRITE);

                if(mtl != null) {
                    FloatTuple diffuseColor = mtl.getKd();

                    if (mtl.useDiffuseColor()) {
                        color = 0xFF000000;

                        for (int i = 0; i < 3; ++i) {
                            color |= (int) (255 * diffuseColor.get(i)) << (16 - 8 * i);
                        }
                    }

                    mtlSprite = getMtlSprite(textureGetter, new Identifier(mtl.getMapKd()));
                }

                for (int i = 0; i < matGroupObj.getNumFaces(); i++) {
                    FloatTuple floatTuple;
                    Vector3f vertex;
                    FloatTuple normal;
                    int v;
                    for (v = 0; v < matGroupObj.getFace(i).getNumVertices(); v++) {
                        floatTuple = matGroupObj.getVertex(matGroupObj.getFace(i).getVertexIndex(v));
                        vertex = new Vector3f(floatTuple.getX(), floatTuple.getY(), floatTuple.getZ());
                        normal = matGroupObj.getNormal(matGroupObj.getFace(i).getNormalIndex(v));

                        addVertex(i, v, vertex, normal, emitter, matGroupObj, false, bakeSettings);

                        // Special conversion of triangles to quads: re-add third vertex as the fourth vertex
                        // Moved into the loop so that `vertex` and `normal` are guaranteed to exist
                        if (v == 2 && matGroupObj.getFace(i).getNumVertices() == 3) {
                            addVertex (i, 3, vertex, normal, emitter, matGroupObj, true, bakeSettings);
                        }
                    }

                    emitter.spriteColor(0, color, color, color, color);
                    emitter.material(RendererAccess.INSTANCE.getRenderer().materialFinder().find());
                    emitter.colorIndex(mtl.getTintIndex());
                    emitter.spriteBake(0, mtlSprite, MutableQuadView.BAKE_NORMALIZED | (bakeSettings.isShaded() ? MutableQuadView.BAKE_LOCK_UV : 0));

                    emitter.emit();
                }
            }

            mesh = builder.build();
        }

        return new OBJBakedModel(mesh, transform, textureGetter.apply(this.sprite));
    }

    private void addVertex(int faceIndex, int vertIndex, Vector3f vertex, FloatTuple normal, QuadEmitter emitter,
                           Obj matGroup, boolean degenerate, ModelBakeSettings bakeSettings) {
        int textureCoordIndex = vertIndex;
        if (degenerate)
            textureCoordIndex --;

        if (bakeSettings.getRotation() != AffineTransformation.identity() && !degenerate) {
            vertex.add(-0.5F, -0.5F, -0.5F);
            vertex.rotate(bakeSettings.getRotation().getRotation2());
            vertex.add(0.5f, 0.5f, 0.5f);
        }

        emitter.pos   (vertIndex, vertex.getX(), vertex.getY(), vertex.getZ());
        emitter.normal(vertIndex, normal.getX(), normal.getY(), normal.getZ());

        if(obj.getNumTexCoords() > 0) {
            FloatTuple text = matGroup.getTexCoord(matGroup.getFace(faceIndex).getTexCoordIndex(textureCoordIndex));

            emitter.sprite(vertIndex, 0, text.getX(), text.getY());
        }else {
            emitter.nominalFace(Direction.getFacing(normal.getX(), normal.getY(), normal.getZ()));
        }
    }

    private static Sprite getMtlSprite(Function<SpriteIdentifier, Sprite> textureGetter, Identifier name) {
        return textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, name));
    }
}
