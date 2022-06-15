package net.doubledoordev.drgflares.networking;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import net.doubledoordev.drgflares.ClientEventHandler;
import net.doubledoordev.drgflares.capability.FlareDataCap;

public class FlareCountSyncPacket
{
    int flareCount;

    public FlareCountSyncPacket(FriendlyByteBuf buffer)
    {
        flareCount = buffer.readInt();
    }

    public FlareCountSyncPacket(int flareCount)
    {
        this.flareCount = flareCount;
    }

    void encode(FriendlyByteBuf buffer)
    {
        buffer.writeInt(flareCount);
    }

    void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() -> ClientEventHandler.getPlayer().getCapability(FlareDataCap.FLARE_DATA).ifPresent(flareCap -> flareCap.setStoredFlares(flareCount)));
        contextSupplier.get().setPacketHandled(true);
    }

}
