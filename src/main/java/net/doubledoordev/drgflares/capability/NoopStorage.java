package net.doubledoordev.drgflares.capability;

import javax.annotation.Nullable;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;


/**
 * todo: 1.17 remove
 * A no-op implementation of {@link net.minecraftforge.common.capabilities.Capability.IStorage} for capabilities that require custom serialize / deserialization logic
 * Borrowed from TFC.
 *
 * @param <T> The capability class
 */
public class NoopStorage<T> implements Capability.IStorage<T>
{
    @Nullable
    @Override
    public INBT writeNBT(Capability<T> capability, T instance, Direction side)
    {
        throw new UnsupportedOperationException("This storage is non functional. Do not use it.");
    }

    @Override
    public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt)
    {
        throw new UnsupportedOperationException("This storage is non functional. Do not use it.");
    }
}
