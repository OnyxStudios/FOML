package com.github.onyxstudios.foml;

import com.github.onyxstudios.foml.obj.ItemOBJLoader;
import com.github.onyxstudios.foml.obj.OBJLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FOML implements ClientModInitializer {
    public static final String MODID = "foml";
    public static Logger LOGGER = LogManager.getLogger("FOML");

    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(OBJLoader.INSTANCE);
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(ItemOBJLoader.INSTANCE);
    }
}
