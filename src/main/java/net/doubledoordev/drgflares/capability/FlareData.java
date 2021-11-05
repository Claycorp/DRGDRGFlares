package net.doubledoordev.drgflares.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import net.doubledoordev.drgflares.DRGFlaresConfig;

public class FlareData implements ICapabilitySerializable<CompoundNBT>
{
    private final LazyOptional<FlareData> capability = LazyOptional.of(() -> this);

    private int storedFlares;
    private int replenishTickCounter;
    private int flareThrowCoolDown;

    public FlareData()
    {
        this.storedFlares = DRGFlaresConfig.GENERAL.flareQuantity.get();
    }

    public int getStoredFlares()
    {
        return storedFlares;
    }

    public void setStoredFlares(int setToFlares)
    {
        this.storedFlares = setToFlares;
    }

    public int getReplenishTickCounter()
    {
        return replenishTickCounter;
    }

    public void setReplenishTickCounter(int replenishTickCounter)
    {
        this.replenishTickCounter = replenishTickCounter;
    }

    public void incrementReplenishTickCounter()
    {
        replenishTickCounter++;
    }

    public int getFlareThrowCoolDown()
    {
        return flareThrowCoolDown;
    }

    public void setFlareThrowCoolDown(int flareThrowCoolDown)
    {
        this.flareThrowCoolDown = flareThrowCoolDown;
    }

    public void decrementFlareThrowCoolDown()
    {
        flareThrowCoolDown--;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putInt("flareCount", getStoredFlares());
        compoundNBT.putInt("replenishTickCounter", getReplenishTickCounter());
        compoundNBT.putInt("throwCoolDown", getFlareThrowCoolDown());
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        setStoredFlares(nbt.getInt("flareCount"));
        setReplenishTickCounter(nbt.getInt("replenishTickCounter"));
        setFlareThrowCoolDown(nbt.getInt("throwCoolDown"));
    }

    /**
     * Retrieves the Optional handler for the capability requested on the specific side.
     * The return value <strong>CAN</strong> be the same for multiple faces.
     * Modders are encouraged to cache this value, using the listener capabilities of the Optional to
     * be notified if the requested capability get lost.
     *
     * @param cap  capability
     * @param side direction
     * @return The requested an optional holding the requested capability.
     */
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        return FlareDataCap.FLARE_DATA.orEmpty(cap, capability);
    }
}
