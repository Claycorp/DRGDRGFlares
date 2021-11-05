package net.doubledoordev.drgflares.capability;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import net.doubledoordev.drgflares.DRGFlares;

public class FlareDataCap
{
    @CapabilityInject(FlareData.class)
    public static final Capability<FlareData> FLARE_DATA = DRGFlares.notNull();
    public static final ResourceLocation KEY = new ResourceLocation(DRGFlares.MODID, "flare_data");
}
