package net.doubledoordev.drgflares.capability;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class FlareStorage implements Capability.IStorage<FlareCap>
{
    /**
     * Serialize the capability instance to a NBTTag.
     * This allows for a central implementation of saving the data.
     *
     * It is important to note that it is up to the API defining
     * the capability what requirements the 'instance' value must have.
     *
     * Due to the possibility of manipulating internal data, some
     * implementations MAY require that the 'instance' be an instance
     * of the 'default' implementation.
     *
     * Review the API docs for more info.
     *
     * @param capability The Capability being stored.
     * @param instance   An instance of that capabilities interface.
     * @param side       The side of the object the instance is associated with.
     * @return a NBT holding the data. Null if no data needs to be stored.
     */
    @Nullable
    @Override
    public INBT writeNBT(Capability<FlareCap> capability, FlareCap instance, Direction side)
    {
        return instance.serializeNBT();
    }

    /**
     * Read the capability instance from a NBT tag.
     *
     * This allows for a central implementation of saving the data.
     *
     * It is important to note that it is up to the API defining
     * the capability what requirements the 'instance' value must have.
     *
     * Due to the possibility of manipulating internal data, some
     * implementations MAY require that the 'instance' be an instance
     * of the 'default' implementation.
     *
     * Review the API docs for more info.         *
     *
     * @param capability The Capability being stored.
     * @param instance   An instance of that capabilities interface.
     * @param side       The side of the object the instance is associated with.
     * @param nbt        A NBT holding the data. Must not be null, as doesn't make sense to call this function with nothing to read...
     */
    @Override
    public void readNBT(Capability<FlareCap> capability, FlareCap instance, Direction side, INBT nbt)
    {
        instance.deserializeNBT((CompoundNBT) nbt);
    }
}
