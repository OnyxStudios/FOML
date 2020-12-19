package nerdhub.fomltest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Random;

public class FOMLTest implements ModInitializer, ClientModInitializer {

    public static final String MODID = "fomltest";
    public static final Block TEST_BLOCK = new CrystalBlock(FabricBlockSettings.of(Material.STONE).hardness(1.0f).nonOpaque());
    public static final Block CONE_BLOCK = new PointerBlock(FabricBlockSettings.of(Material.STONE).hardness(1F).nonOpaque());

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MODID, "test"), TEST_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MODID, "test"), new BlockItem(TEST_BLOCK, new Item.Settings().group(ItemGroup.REDSTONE)));
        Registry.register(Registry.BLOCK, new Identifier(MODID, "cone"), CONE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MODID, "cone"), new BlockItem(CONE_BLOCK, new Item.Settings().group(ItemGroup.REDSTONE)));
    }

    private static final Random RANDOM = new Random();

    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.BLOCK.register(((blockState, blockRenderView, blockPos, i) -> {
            RANDOM.setSeed(blockPos.asLong());

            return RANDOM.nextBoolean() ? 0xFF00FF : 0x0088FF;
        }), TEST_BLOCK);
    }
}
