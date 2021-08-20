package net.doubledoordev.drgflares.networking;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import net.doubledoordev.drgflares.ClientEventHandler;
import net.doubledoordev.drgflares.capability.FlareProvider;

public class FlareCountSyncPacket
{
    int flareCount;

    public FlareCountSyncPacket(PacketBuffer buffer)
    {
        flareCount = buffer.readInt();
    }

    public FlareCountSyncPacket(int flareCount)
    {
        this.flareCount = flareCount;
    }

    void encode(PacketBuffer buffer)
    {
        buffer.writeInt(flareCount);
    }

    void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() -> {
            ClientEventHandler.getPlayer().getCapability(FlareProvider.FLARE_CAP_CAPABILITY).ifPresent(flareCap -> flareCap.setStoredFlares(flareCount));
        });
        contextSupplier.get().setPacketHandled(true);
    }

}
