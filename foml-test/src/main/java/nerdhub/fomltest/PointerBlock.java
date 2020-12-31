package nerdhub.fomltest;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class PointerBlock extends HorizontalFacingBlock {
    public static VoxelShape OUTLINE = Block.createCuboidShape(
            3, 5, 3, 13, 11,13
    );

    public PointerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext shapeContext) {
        return OUTLINE;
    }
}
