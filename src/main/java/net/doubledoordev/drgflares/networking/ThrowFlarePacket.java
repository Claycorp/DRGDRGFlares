package net.doubledoordev.drgflares.networking;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import net.doubledoordev.drgflares.DRGFlares;
import net.doubledoordev.drgflares.capability.FlareData;
import net.doubledoordev.drgflares.capability.FlareDataCap;
import net.doubledoordev.drgflares.entity.FlareEntity;

import static net.doubledoordev.drgflares.DRGFlaresConfig.GENERALCONFIG;

public class ThrowFlarePacket
{
    public void shootFlare(Level level, ServerPlayer player, FlareData flareData)
    {
        if (GENERALCONFIG.makeNoiseWhenThrown.get())
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.5F, 0.4F / level.getRandom().nextFloat() * 0.4F + 0.8F);
        FlareEntity flareEntity = new FlareEntity(level, player);
        //Last 3 values in order, ???, throw force, force deviation from origin higher = less accurate.
        flareEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, GENERALCONFIG.flareThrowForce.get().floatValue(), 1.0F);
        //Special handling for random colored flares.
        switch (flareData.getFlareColor())
        {
            //Spaced colors. "Fixed rainbow"
            case -2 -> flareEntity.setColor(DRGFlares.HSBtoRGB((flareData.getFlaresThrown() / 360F * 100) * .1f, 1, 1));

            //Random colors.
            case -4 -> flareEntity.setColor(flareEntity.level.random.nextInt(0xFFFFFF));
            default -> flareEntity.setColor(flareData.getFlareColor());
        }
        flareEntity.setOwner(player);
        level.addFreshEntity(flareEntity);
        flareData.incrementThrownFlares();
    }

    void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();

        //Must come from a player.
        if (player != null)
        {
            Level level = context.getSender().level;
            //Must be on a server.
            if (!level.isClientSide)
            {
                //Now we check for the capability to edit it as it's required.
                player.getCapability(FlareDataCap.FLARE_DATA).ifPresent(flareCap -> {
                    if (flareCap.getStoredFlares() > 0 && flareCap.getFlareThrowCoolDown() == 0)
                    {
                        if (flareCap.getFlareColor() == -1)
                            flareCap.setFlareColor(DRGFlares.stringToColorInt("#c334eb"));

                        //If the player is creative throw free flares. If they are spectator & are allowed & they don't need to generate flares, throw em.
                        if (player.isCreative() || (player.isSpectator() && GENERALCONFIG.spectatorsThrowFlares.get() && !GENERALCONFIG.spectatorsRequiredToGenerateFlares.get()))
                        {
                            shootFlare(level, player, flareCap);
                        }
                        else
                        {
                            shootFlare(level, player, flareCap);
                            flareCap.setStoredFlares(flareCap.getStoredFlares() - 1);
                            flareCap.setFlareThrowCoolDown(GENERALCONFIG.flareThrowCoolDown.get());
                            PacketHandler.send(PacketDistributor.PLAYER.with(context::getSender), new FlareCountSyncPacket(flareCap.getStoredFlares()));
                        }
                    }
                });
            }
        }
    }
}
