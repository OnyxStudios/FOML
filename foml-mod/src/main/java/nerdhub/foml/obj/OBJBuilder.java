package nerdhub.foml.obj;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Mtl;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjSplitting;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
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
    private Function<SpriteIdentifier, Sprite> textureGetter;

    public OBJBuilder(Obj obj, List<Mtl> mtlList) {
        meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        quadEmitter = meshBuilder.getEmitter();
        this.obj = obj;
        this.mtlList = mtlList;
    }

    private void addVertex (int faceIndex, int vertIndex, FloatTuple vertex, FloatTuple normal, QuadEmitter emitter,
                            Sprite mtlSprite, Obj matGroup, boolean degenerate) {
        int textureCoordIndex = vertIndex;
        if (degenerate)
            textureCoordIndex --;

        quadEmitter.pos   (vertIndex, vertex.getX(), vertex.getY(), vertex.getZ());
        quadEmitter.normal(vertIndex, normal.getX(), normal.getY(), normal.getZ());

        if(obj.getNumTexCoords() > 0) {
            FloatTuple text = matGroup.getTexCoord(matGroup.getFace(faceIndex).getTexCoordIndex(textureCoordIndex));

            quadEmitter.sprite(vertIndex, 0, text.getX(), 1 - text.getY());
        }else {
            quadEmitter.nominalFace(Direction.getFacing(normal.getX(), normal.getY(), normal.getZ()));
        }
    }

    public Mesh build() {
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
                FloatTuple vertex = null;
                FloatTuple normal = null;
                int v;
                for (v = 0; v < matGroupObj.getFace(i).getNumVertices(); v++) {
                    vertex = matGroupObj.getVertex(matGroupObj.getFace(i).getVertexIndex(v));
                    normal = matGroupObj.getNormal(matGroupObj.getFace(i).getNormalIndex(v));

                    addVertex (i, v, vertex, normal, quadEmitter, mtlSprite, matGroupObj, false);
                }

                // Special conversion of triangles to quads: re-add third vertex as the fourth vertex
                if (matGroupObj.getFace(i).getNumVertices() == 3) {
                    addVertex (i, 3, vertex, normal, quadEmitter, mtlSprite, matGroupObj, true);
                }

                quadEmitter.spriteColor(0, -1, -1, -1, -1);
                quadEmitter.material(RendererAccess.INSTANCE.getRenderer().materialFinder().find());
                quadEmitter.colorIndex(1);
                quadEmitter.spriteBake(0, mtlSprite, MutableQuadView.BAKE_NORMALIZED);

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
}
