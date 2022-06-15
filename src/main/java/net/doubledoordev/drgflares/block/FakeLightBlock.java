package net.doubledoordev.drgflares.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.doubledoordev.drgflares.DRGFlaresConfig;

public class FakeLightBlock extends BaseEntityBlock
{
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public FakeLightBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LIT);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public RenderShape getRenderShape(BlockState state)
    {
        // Do this so we can get visible light blocks.
        if (DRGFlaresConfig.GENERALCONFIG.lightBlockDebug.get())
        {
            return RenderShape.MODEL;
        }
        else return RenderShape.INVISIBLE;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @ParametersAreNonnullByDefault
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos)
    {
        return Shapes.empty();
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos)
    {
        return 1.0F;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @ParametersAreNonnullByDefault
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext)
    {
        return Shapes.empty();
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos)
    {
        return DRGFlaresConfig.GENERALCONFIG.flareLightLevel.get();
    }

    @ParametersAreNonnullByDefault
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FakeLightBlockEntity(pos, state);
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, BlockRegistry.FAKE_LIGHT_BE.get(), FakeLightBlockEntity::tick);
    }
}
