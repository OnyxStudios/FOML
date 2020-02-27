package nerdhub.fomltest.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;

public class FriendEntity extends CreeperEntity {
    public FriendEntity(EntityType<? extends FriendEntity> entityType, World world) {
        super(entityType, world);
    }
}
