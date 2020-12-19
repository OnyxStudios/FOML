package nerdhub.fomltest;

import dev.onyxstudios.foml.obj.OBJLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FOMLTest implements ModInitializer, ClientModInitializer {

    public static final String MODID = "fomltest";
    public static Block TEST_BLOCK = new Block(FabricBlockSettings.of(Material.STONE).hardness(1.0f).nonOpaque().build());

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MODID, "test"), TEST_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MODID, "test"), new BlockItem(TEST_BLOCK, new Item.Settings().group(ItemGroup.REDSTONE)));
    }

    @Override
    public void onInitializeClient() {
        OBJLoader.INSTANCE.registerDomain(MODID);
    }
}
