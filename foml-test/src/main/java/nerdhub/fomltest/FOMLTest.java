package nerdhub.fomltest;

import nerdhub.foml.ManualModelLoaderRegistry;
import nerdhub.foml.obj.OBJLoader;
import nerdhub.fomltest.entity.FriendEntity;
import nerdhub.fomltest.entity.FriendEntityModel;
import nerdhub.fomltest.entity.FriendEntityRenderer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FOMLTest implements ModInitializer {

    public static final String MODID = "fomltest";
    public static Block TEST_BLOCK = new Block(FabricBlockSettings.of(Material.STONE).hardness(1.0f).build());

    public static final Identifier FRIEND_ENTITY_ID = new Identifier(MODID, "friend");
    public static final EntityType<FriendEntity> FRIEND_ENTITY_TYPE = new EntityType<>(FriendEntity::new, EntityCategory.MONSTER, true, true, false, true, EntityDimensions.fixed(0.5f, 1.8f));

    @Override
    public void onInitialize() {
        OBJLoader.INSTANCE.registerDomain(MODID);
        Registry.register(Registry.BLOCK, new Identifier(MODID, "test"), TEST_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MODID, "test"), new BlockItem(TEST_BLOCK, new Item.Settings().group(ItemGroup.REDSTONE)));

        ManualModelLoaderRegistry.INSTANCE.register(FriendEntityModel.MODEL);
        Registry.register(Registry.ENTITY_TYPE, FRIEND_ENTITY_ID, FRIEND_ENTITY_TYPE);
        EntityRendererRegistry.INSTANCE.register(FRIEND_ENTITY_TYPE, (dispatcher, context) -> new FriendEntityRenderer<>(dispatcher, new FriendEntityModel<>()));
    }
}
