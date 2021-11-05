package net.doubledoordev.drgflares.networking;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import net.doubledoordev.drgflares.DRGFlaresConfig;
import net.doubledoordev.drgflares.capability.FlareDataCap;
import net.doubledoordev.drgflares.entity.FlareEntity;

public class ThrowFlarePacket
{
    void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayerEntity player = context.getSender();

        if (player != null)
        {
            World world = context.getSender().level;
            if (!world.isClientSide)
            {
                player.getCapability(FlareDataCap.FLARE_DATA).ifPresent(flareCap -> {
                    if (flareCap.getStoredFlares() > 0)
                    {
                        if (DRGFlaresConfig.GENERAL.makeNoiseWhenThrown.get())
                            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundCategory.PLAYERS, 0.5F, 0.4F / world.getRandom().nextFloat() * 0.4F + 0.8F);
                        FlareEntity flareEntity = new FlareEntity(world, player);
                        flareEntity.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 1.0F);
                        world.addFreshEntity(flareEntity);
                        if (!player.isCreative())
                            flareCap.setStoredFlares(flareCap.getStoredFlares() - 1);
                        PacketHandler.send(PacketDistributor.PLAYER.with(context::getSender), new FlareCountSyncPacket(flareCap.getStoredFlares()));
                    }
                });
            }
        }
    }
}
