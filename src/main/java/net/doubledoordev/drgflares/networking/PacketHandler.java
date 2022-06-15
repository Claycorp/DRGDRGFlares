package net.doubledoordev.drgflares.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import static net.minecraftforge.versions.forge.ForgeVersion.MOD_ID;

public class PacketHandler
{
    private static final String VERSION = Integer.toString(1);
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID), () -> VERSION, VERSION::equals, VERSION::equals);

    public static void send(PacketDistributor.PacketTarget target, Object message)
    {
        CHANNEL.send(target, message);
    }

    public static SimpleChannel get()
    {
        return CHANNEL;
    }

    @SuppressWarnings("UnusedAssignment")
    public static void init()
    {
        int id = 0;

        CHANNEL.registerMessage(id++, ThrowFlarePacket.class, (packet, buf) -> {}, buffer -> new ThrowFlarePacket(), ThrowFlarePacket::handle);
        CHANNEL.registerMessage(id++, FlareCountSyncPacket.class, FlareCountSyncPacket::encode, FlareCountSyncPacket::new, FlareCountSyncPacket::handle);
    }
}
