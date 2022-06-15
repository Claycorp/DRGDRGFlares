package net.doubledoordev.drgflares.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import net.doubledoordev.drgflares.DRGFlares;

public class FlareDataCap
{
    public static final Capability<FlareData> FLARE_DATA = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation KEY = new ResourceLocation(DRGFlares.MODID, "flare_data");
}
