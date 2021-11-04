package net.doubledoordev.drgflares.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.doubledoordev.drgflares.DRGFlaresConfig;

public class FakeLightBlock extends Block
{
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public FakeLightBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean propagatesSkylightDown(BlockState state, IBlockReader blockReader, BlockPos pos)
    {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(LIT);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public BlockRenderType getRenderShape(BlockState state)
    {
        // Do this so we can get visible light blocks.
        if (DRGFlaresConfig.GENERAL.lightBlockDebug.get())
        {
            return BlockRenderType.MODEL;
        }
        else return BlockRenderType.INVISIBLE;
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    @Override
    public VoxelShape getInteractionShape(BlockState state, IBlockReader world, BlockPos pos)
    {
        return VoxelShapes.empty();
    }

    @ParametersAreNonnullByDefault
    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState state, IBlockReader blockReader, BlockPos pos)
    {
        return 1.0F;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        return DRGFlaresConfig.GENERAL.flareLightLevel.get();
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new FakeLightBlockEntity();
    }
}
