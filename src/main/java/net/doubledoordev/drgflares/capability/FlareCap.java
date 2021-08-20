package net.doubledoordev.drgflares.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import net.doubledoordev.drgflares.DRGFlaresConfig;

public class FlareCap implements INBTSerializable<CompoundNBT>
{
    private int storedFlares;
    private int replenishTickCounter;

    public FlareCap()
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

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putInt("flareCount", getStoredFlares());
        compoundNBT.putInt("replenishTickCounter", getReplenishTickCounter());
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        setStoredFlares(nbt.getInt("flareCount"));
        setReplenishTickCounter(nbt.getInt("replenishTickCounter"));
    }
}
