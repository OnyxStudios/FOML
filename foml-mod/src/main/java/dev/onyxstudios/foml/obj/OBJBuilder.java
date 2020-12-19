package dev.onyxstudios.foml.obj;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Mtl;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjSplitting;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OBJBuilder {

    public static final SpriteIdentifier DEFAULT_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, null);

    private MeshBuilder meshBuilder;
    private QuadEmitter quadEmitter;

    private final Obj obj;
    private final List<Mtl> mtlList;
    private final SpriteIdentifier sprite;
    private Function<SpriteIdentifier, Sprite> textureGetter;

    public OBJBuilder(Obj obj, List<Mtl> mtlList) {
        meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        quadEmitter = meshBuilder.getEmitter();
        this.obj = obj;
        this.mtlList = mtlList;

        Mtl mtl = this.findMtlForName("sprite");
        this.sprite = mtlList.size() > 0
                ? new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, new Identifier((mtl == null ? mtlList.get(0) : mtl).getMapKd()))
                : DEFAULT_SPRITE;

    }

    private void addVertex(int faceIndex, int vertIndex, Vector3f vertex, FloatTuple normal, QuadEmitter emitter,
                           Sprite mtlSprite, Obj matGroup, boolean degenerate, ModelBakeSettings bakeSettings) {
        int textureCoordIndex = vertIndex;
        if (degenerate)
            textureCoordIndex --;

        if (bakeSettings.getRotation() != AffineTransformation.identity() && !degenerate) {
            vertex.add(-0.5F, -0.5F, -0.5F);
            vertex.rotate(bakeSettings.getRotation().getRotation2());
            vertex.add(0.5f, 0.5f, 0.5f);
        }

        quadEmitter.pos   (vertIndex, vertex.getX(), vertex.getY(), vertex.getZ());
        quadEmitter.normal(vertIndex, normal.getX(), normal.getY(), normal.getZ());

        if(obj.getNumTexCoords() > 0) {
            FloatTuple text = matGroup.getTexCoord(matGroup.getFace(faceIndex).getTexCoordIndex(textureCoordIndex));

            quadEmitter.sprite(vertIndex, 0, text.getX(), text.getY());
        }else {
            quadEmitter.nominalFace(Direction.getFacing(normal.getX(), normal.getY(), normal.getZ()));
        }
    }

    public Mesh build(ModelBakeSettings bakeSettings) {
        Map<String, Obj> materialGroups = ObjSplitting.splitByMaterialGroups(obj);

        for (Map.Entry<String, Obj> entry : materialGroups.entrySet()) {
            String matName = entry.getKey();
            Obj matGroupObj = entry.getValue();


            Mtl mtl = findMtlForName(matName);
            FloatTuple diffuseColor = null;
            FloatTuple specularColor = null;
            Sprite mtlSprite = textureGetter.apply(DEFAULT_SPRITE);

            if(mtl != null) {
                diffuseColor = mtl.getKd();
                specularColor = mtl.getKs();
                mtlSprite = getMtlSprite(new Identifier(mtl.getMapKd()));
            }

            for (int i = 0; i < matGroupObj.getNumFaces(); i++) {
                FloatTuple floatTuple = null;
                Vector3f vertex = null;
                FloatTuple normal = null;
                int v;
                for (v = 0; v < matGroupObj.getFace(i).getNumVertices(); v++) {
                    floatTuple = matGroupObj.getVertex(matGroupObj.getFace(i).getVertexIndex(v));
                    vertex = new Vector3f(floatTuple.getX(), floatTuple.getY(), floatTuple.getZ());
                    normal = matGroupObj.getNormal(matGroupObj.getFace(i).getNormalIndex(v));

                    addVertex(i, v, vertex, normal, quadEmitter, mtlSprite, matGroupObj, false, bakeSettings);
                }

                // Special conversion of triangles to quads: re-add third vertex as the fourth vertex
                if (matGroupObj.getFace(i).getNumVertices() == 3) {
                    addVertex (i, 3, vertex, normal, quadEmitter, mtlSprite, matGroupObj, true, bakeSettings);
                }

                quadEmitter.spriteColor(0, -1, -1, -1, -1);
                quadEmitter.material(RendererAccess.INSTANCE.getRenderer().materialFinder().find());
                quadEmitter.colorIndex(1);
                quadEmitter.spriteBake(0, mtlSprite, MutableQuadView.BAKE_NORMALIZED | (bakeSettings.isShaded() ? MutableQuadView.BAKE_LOCK_UV : 0));

                quadEmitter.emit();
            }
        }

        return meshBuilder.build();
    }

    public List<Mtl> getMtlList() {
        return mtlList;
    }

    public Mtl findMtlForName(String name) {
        for (Mtl mtl : mtlList) {
            if(mtl.getName().equals(name)) {
                return mtl;
            }
        }

        return null;
    }

    public Sprite getMtlSprite(Identifier name) {
        return textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, name));
    }

    public void setTextureGetter(Function<SpriteIdentifier, Sprite> textureGetter) {
        this.textureGetter = textureGetter;
    }

    public SpriteIdentifier getSprite() {
        return this.sprite;
    }
}
