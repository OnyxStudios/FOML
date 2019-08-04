package nerdhub.foml.obj;

import de.javagl.obj.*;
import nerdhub.foml.FOML;
import nerdhub.foml.obj.baked.OBJBakedModel;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OBJLoader {

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

    public OBJBakedModel loadModel(Reader reader, String modid, ResourceManager manager) {
        OBJBuilder model;
        try {
            Obj obj = ObjUtils.convertToRenderable(ObjReader.read(reader));
            model = new OBJBuilder(ObjUtils.triangulate(obj), loadMTL(manager, modid, obj.getMtlFileNames()));
        } catch (IOException e) {
            FOML.LOGGER.error("Could not read obj model!", e);
            return null;
        }

        return model.build();
    }

    public List<Mtl> loadMTL(ResourceManager manager, String modid, List<String> mtlNames) throws IOException {
        List<Mtl> mtls = new ArrayList<>();

        for (String name : mtlNames) {
            Identifier resourceId = new Identifier(modid, "models/block/" + name);
            if(manager.containsResource(resourceId)) {
                Resource resource = manager.getResource(resourceId);
                mtls.addAll(MtlReader.read(resource.getInputStream()));
            }else {
                FOML.LOGGER.warn("Warning, a model specifies an MTL File but it could not be found! Source: " + modid + ":" + name);
            }
        }

        return mtls;
    }
}
