package net.doubledoordev.drgflares.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import net.doubledoordev.drgflares.DRGFlares;
import net.doubledoordev.drgflares.DRGFlaresConfig;

public class FlareData implements ICapabilitySerializable<CompoundTag>
{
    private final LazyOptional<FlareData> capability = LazyOptional.of(() -> this);

    private int storedFlares;
    private int replenishTickCounter;
    private int flareThrowCoolDown;
    private int flareColor;
    private int flaresThrown;

    public FlareData()
    {
        this.storedFlares = DRGFlaresConfig.GENERALCONFIG.flareQuantity.get();
        this.flareColor = DRGFlares.stringToColorInt(DRGFlaresConfig.GENERALCONFIG.flareCoreColor.get());
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

    public int getFlareColor()
    {
        return flareColor;
    }

    public void setFlareColor(int flareColor)
    {
        this.flareColor = flareColor;
    }

    public int getFlaresThrown()
    {
        return flaresThrown;
    }

    public void setFlaresThrown(int flaresThrown)
    {
        this.flaresThrown = flaresThrown;
    }

    public void incrementThrownFlares()
    {
        this.flaresThrown++;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("flareCount", getStoredFlares());
        compoundTag.putInt("replenishTickCounter", getReplenishTickCounter());
        compoundTag.putInt("throwCoolDown", getFlareThrowCoolDown());
        compoundTag.putInt("color", getFlareColor());
        compoundTag.putInt("flaresThrown", getFlaresThrown());
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag)
    {
        setStoredFlares(compoundTag.getInt("flareCount"));
        setReplenishTickCounter(compoundTag.getInt("replenishTickCounter"));
        setFlareThrowCoolDown(compoundTag.getInt("throwCoolDown"));
        setFlareColor(compoundTag.getInt("color"));
        setFlaresThrown(compoundTag.getInt("flaresThrown"));
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
