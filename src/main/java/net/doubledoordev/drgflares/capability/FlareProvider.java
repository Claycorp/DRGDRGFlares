package net.doubledoordev.drgflares.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class FlareProvider implements ICapabilityProvider, INBTSerializable<INBT>
{
    @CapabilityInject(FlareCap.class)
    public static final Capability<FlareCap> FLARE_CAP_CAPABILITY = null;

    private final FlareCap flareContainer = new FlareCap();

    private final LazyOptional<FlareCap> flareInstance = LazyOptional.of(() -> flareContainer);

    @Override
    public INBT serializeNBT()
    {
        return flareContainer.serializeNBT();
    }

    @Override
    public void deserializeNBT(INBT nbt)
    {
        flareContainer.deserializeNBT((CompoundNBT) nbt);
    }

    /**
     * Retrieves the Optional handler for the capability requested on the specific side.
     * The return value <strong>CAN</strong> be the same for multiple faces.
     * Modders are encouraged to cache this value, using the listener capabilities of the Optional to
     * be notified if the requested capability get lost.
     *
     * @param cap
     * @param side
     * @return The requested an optional holding the requested capability.
     */
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == FLARE_CAP_CAPABILITY)
            return (LazyOptional<T>) flareInstance;
        return LazyOptional.empty();
    }
}
