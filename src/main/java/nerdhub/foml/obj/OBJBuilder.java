package nerdhub.foml.obj;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Mtl;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjSplitting;
import nerdhub.foml.obj.baked.OBJBakedModel;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Map;

public class OBJBuilder {

    private MeshBuilder meshBuilder;
    private QuadEmitter quadEmitter;

    private final Obj obj;
    private final List<Mtl> mtlList;
    private Sprite sprite;

    public OBJBuilder(Obj obj, List<Mtl> mtlList) {
        meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        quadEmitter = meshBuilder.getEmitter();
        this.obj = obj;
        this.mtlList = mtlList;
        this.sprite = MinecraftClient.getInstance().getSpriteAtlas().getSprite(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
    }

    public OBJBakedModel build() {
        Map<String, Obj> materialGroups = ObjSplitting.splitByMaterialGroups(obj);

        for (Map.Entry<String, Obj> entry : materialGroups.entrySet()) {
            String matName = entry.getKey();
            Obj matGroupObj = entry.getValue();


            Mtl mtl = findMtlForName(matName);
            FloatTuple diffuseColor = null;
            FloatTuple specularColor = null;
            Sprite mtlSprite;

            if(mtl != null) {
                diffuseColor = mtl.getKd();
                specularColor = mtl.getKs();
                //try {
                //TODO REGISTER RESOURCES FROM MTL WHEN LOADING THE MODEL
                //MinecraftClient.getInstance().getSpriteAtlas().getSprite(new Identifier(mtl.getMapKd()));
                //mtlSprite = manager.containsResource(new Identifier(mtl.getMapKd())) ? manager.getResource(new Identifier(mtl.getMapKd())) : sprite;
                //} catch (IOException e) {
                //    e.printStackTrace();
                //}
            }

            for (int i = 0; i < matGroupObj.getNumFaces(); i++) {

                FloatTuple vertex = null;
                FloatTuple normal = null;
                int v;
                for (v = 0; v < matGroupObj.getFace(i).getNumVertices(); v++) {
                    vertex = matGroupObj.getVertex(matGroupObj.getFace(i).getVertexIndex(v));
                    normal = matGroupObj.getNormal(matGroupObj.getFace(i).getNormalIndex(v));

                    quadEmitter.pos(v, vertex.getX(), vertex.getY(), vertex.getZ());
                    quadEmitter.normal(v + 1, normal.getX(), normal.getY(), normal.getZ());

                    if(obj.getNumTexCoords() > 0) {
                        FloatTuple text = matGroupObj.getTexCoord(matGroupObj.getFace(i).getTexCoordIndex(v));
                        quadEmitter.sprite(v, 0, text.getX(), text.getY());
                        quadEmitter.spriteBake(0, sprite, quadEmitter.BAKE_ROTATE_NONE);
                        quadEmitter.spriteColor(0, 1, 1, 1, 1);
                    }else {
                        quadEmitter.nominalFace(Direction.getFacing(normal.getX(), normal.getY(), normal.getZ()));
                    }
                    quadEmitter.colorIndex(1);
                }

                if(vertex != null && normal != null) {
                    quadEmitter.pos(v + 1, vertex.getX(), vertex.getY(), vertex.getZ());
                    quadEmitter.normal(v + 2, normal.getX(), normal.getY(), normal.getZ());
                }



                quadEmitter.emit();
            }
        }

        return new OBJBakedModel(meshBuilder.build(), sprite);
    }

    public Mtl findMtlForName(String name) {
        for (Mtl mtl : mtlList) {
            if(mtl.getName().equals(name)) {
                return mtl;
            }
        }

        return null;
    }
}
