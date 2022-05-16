package net.doubledoordev.drgflares.networking;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import net.doubledoordev.drgflares.DRGFlares;
import net.doubledoordev.drgflares.capability.FlareData;
import net.doubledoordev.drgflares.capability.FlareDataCap;
import net.doubledoordev.drgflares.entity.FlareEntity;

import static net.doubledoordev.drgflares.DRGFlaresConfig.GENERALCONFIG;

public class ThrowFlarePacket
{
    public void shootFlare(World world, ServerPlayerEntity player, FlareData flareData)
    {
        if (GENERALCONFIG.makeNoiseWhenThrown.get())
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundCategory.PLAYERS, 0.5F, 0.4F / world.getRandom().nextFloat() * 0.4F + 0.8F);
        FlareEntity flareEntity = new FlareEntity(world, player);
        //Last 3 values in order, ???, throw force, force deviation from origin higher = less accurate.
        flareEntity.shootFromRotation(player, player.xRot, player.yRot, 0.0F, GENERALCONFIG.flareThrowForce.get().floatValue(), 1.0F);
        //Special handling for random colored flares.
        switch (flareData.getFlareColor())
        {
            //Spaced colors. "Fixed rainbow"
            case 1:
                flareEntity.setColor(DRGFlares.HSBtoRGB((flareData.getFlaresThrown() / 360F * 100) * .1f, 1, 1));
                break;
            //Random colors.
            case 3:
                flareEntity.setColor(flareEntity.level.random.nextInt(0xFFFFFF));
                break;
            default:
                flareEntity.setColor(flareData.getFlareColor());
        }
        flareEntity.setOwner(player);
        world.addFreshEntity(flareEntity);
        flareData.incrementThrownFlares();
    }

    void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayerEntity player = context.getSender();

        //Must come from a player.
        if (player != null)
        {
            World world = context.getSender().level;
            //Must be on a server.
            if (!world.isClientSide)
            {
                //Now we check for the capability to edit it as it's required.
                player.getCapability(FlareDataCap.FLARE_DATA).ifPresent(flareCap -> {
                    if (flareCap.getStoredFlares() > 0 && flareCap.getFlareThrowCoolDown() == 0)
                    {
                        if (flareCap.getFlareColor() == 0)
                            flareCap.setFlareColor(DRGFlares.stringToColorInt("#c334eb"));

                        //If the player is creative throw free flares. If they are spectator & are allowed & they don't need to generate flares, throw em.
                        if (player.isCreative() || (player.isSpectator() && GENERALCONFIG.spectatorsThrowFlares.get() && !GENERALCONFIG.spectatorsRequiredToGenerateFlares.get()))
                        {
                            shootFlare(world, player, flareCap);
                        }
                        else
                        {
                            shootFlare(world, player, flareCap);
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
