package net.doubledoordev.drgflares.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import net.doubledoordev.drgflares.DRGFlaresConfig;

public class FakeLightBlockEntity extends TileEntity implements ITickableTileEntity
{
    int lightDecayTime = DRGFlaresConfig.GENERAL.lightDecayTime.get();
    int nextCheckIn = DRGFlaresConfig.GENERAL.noSourceDecayTime.get();
    int tickCounter;

    public FakeLightBlockEntity(TileEntityType<?> tileEntityType)
    {
        super(tileEntityType);
    }

    public FakeLightBlockEntity()
    {
        this(BlockRegistry.FAKE_LIGHT_BE.get());
    }

    @Override
    public void tick()
    {
        if (!(level != null && level.isClientSide()))
        {
            BlockState state = level != null ? level.getBlockState(worldPosition) : null;

            // Make sure our block is our block.
            if (state != null && state.getBlock().is(BlockRegistry.FAKE_LIGHT.get()))
            {
                // Check if we lost our entity for too long & remove block dead.
                if (nextCheckIn == 0)
                {
                    level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
                    level.removeBlockEntity(worldPosition);
                }

                // Check our light decay time, turn it off after it's dead.
                if (lightDecayTime <= tickCounter)
                {
                    level.setBlockAndUpdate(worldPosition, state.setValue(FakeLightBlock.LIT, false));
                    setChanged();
                }
                tickCounter++;

                // Prevent the check from going negative.
                if (nextCheckIn > 0)
                    nextCheckIn--;
            }
            else
            {
                if ((level != null ? level.getBlockEntity(worldPosition) : null) != null)
                    level.removeBlockEntity(worldPosition);
            }
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void load(BlockState state, CompoundNBT compoundNBT)
    {
        super.load(state, compoundNBT);
        lightDecayTime = compoundNBT.getInt("lightDecayTime");
        nextCheckIn = compoundNBT.getInt("nextCheckIn");
        tickCounter = compoundNBT.getInt("tickCounter");
    }

    @Override
    @Nonnull
    public CompoundNBT save(CompoundNBT compoundNBT)
    {
        compoundNBT.putInt("lightDecayTime", lightDecayTime);
        compoundNBT.putInt("nextCheckIn", nextCheckIn);
        compoundNBT.putInt("tickCounter", tickCounter);

        return super.save(compoundNBT);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.worldPosition, 3, this.getUpdateTag());
    }

    @Override
    @Nonnull
    public CompoundNBT getUpdateTag()
    {
        return this.save(new CompoundNBT());
    }

    /**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        deserializeNBT(pkt.getTag());
        setChanged();
    }

    public void setNextCheckIn(int nextCheckIn)
    {
        this.nextCheckIn = nextCheckIn;
    }
}
