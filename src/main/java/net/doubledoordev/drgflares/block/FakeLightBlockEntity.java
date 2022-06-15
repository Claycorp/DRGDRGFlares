package net.doubledoordev.drgflares.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.doubledoordev.drgflares.DRGFlaresConfig;

public class FakeLightBlockEntity extends BlockEntity
{
    int lightDecayTime = DRGFlaresConfig.GENERALCONFIG.lightDecayTime.get();
    int nextCheckIn = DRGFlaresConfig.GENERALCONFIG.noSourceDecayTime.get();
    int tickCounter;

    public static void tick(Level level, BlockPos pos, BlockState state, FakeLightBlockEntity blockEntity)
    {
        if (level != null && !level.isClientSide())
        {
            // Make sure our block is our block.
            if (state.is(BlockRegistry.FAKE_LIGHT.get()))
            {
                // Check if we lost our entity for too long & remove block dead.
                if (blockEntity.nextCheckIn == 0)
                {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    level.removeBlockEntity(pos);
                }

                // Check our light decay time, turn it off after it's dead.
                if (blockEntity.lightDecayTime <= blockEntity.tickCounter)
                {
                    level.setBlockAndUpdate(pos, state.setValue(FakeLightBlock.LIT, false));
                    blockEntity.setChanged();
                }
                blockEntity.tickCounter++;

                // Prevent the check from going negative.
                if (blockEntity.nextCheckIn > 0)
                    blockEntity.nextCheckIn--;
            }
            else
            {
                if (level.getBlockEntity(pos) != null)
                    level.removeBlockEntity(pos);
            }
        }
    }

    public FakeLightBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockRegistry.FAKE_LIGHT_BE.get(), pos, state);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void load(CompoundTag compoundTag)
    {
        super.load(compoundTag);
        lightDecayTime = compoundTag.getInt("lightDecayTime");
        nextCheckIn = compoundTag.getInt("nextCheckIn");
        tickCounter = compoundTag.getInt("tickCounter");
    }

    @Override
    @ParametersAreNonnullByDefault
    public void saveAdditional(CompoundTag compoundTag)
    {
        super.saveAdditional(compoundTag);
        compoundTag.putInt("lightDecayTime", lightDecayTime);
        compoundTag.putInt("nextCheckIn", nextCheckIn);
        compoundTag.putInt("tickCounter", tickCounter);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag()
    {
        return this.saveWithFullMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        deserializeNBT(pkt.getTag());
        setChanged();
    }

    public void setNextCheckIn(int nextCheckIn)
    {
        this.nextCheckIn = nextCheckIn;
    }
}
