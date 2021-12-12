package net.doubledoordev.drgflares;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.PacketDistributor;

import net.doubledoordev.drgflares.block.BlockRegistry;
import net.doubledoordev.drgflares.capability.FlareData;
import net.doubledoordev.drgflares.capability.FlareDataCap;
import net.doubledoordev.drgflares.capability.NoopStorage;
import net.doubledoordev.drgflares.entity.EntityRegistry;
import net.doubledoordev.drgflares.networking.FlareCountSyncPacket;
import net.doubledoordev.drgflares.networking.PacketHandler;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("drgflares")
@Mod.EventBusSubscriber
public class DRGFlares
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "drgflares";
    private static final ResourceLocation CAPABILITY_FLARES = new ResourceLocation("drgflares", "flares");

    // Attaches the Flare capability to the player.
    @SubscribeEvent
    public static void attachFlareCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof PlayerEntity)
        {
            event.addCapability(CAPABILITY_FLARES, new FlareData());
        }
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.side.isServer() && event.phase.equals(TickEvent.Phase.START))
            event.player.getCapability(FlareDataCap.FLARE_DATA).ifPresent(flareCap -> {
                int storedFlares = flareCap.getStoredFlares();
                int maxFlares = DRGFlaresConfig.GENERAL.flareQuantity.get();

                if (event.player.isCreative() || (event.player.isSpectator() && !DRGFlaresConfig.GENERAL.spectatorsRequiredToGenerateFlares.get()) && flareCap.getStoredFlares() != maxFlares)
                {
                    flareCap.setStoredFlares(maxFlares);
                    flareCap.setFlareThrowCoolDown(0);
                    flareCap.setReplenishTickCounter(0);
                    PacketHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.player), new FlareCountSyncPacket(maxFlares));
                    return;
                }

                if (flareCap.getReplenishTickCounter() >= DRGFlaresConfig.GENERAL.flareReplenishTime.get() && storedFlares < maxFlares)
                {
                    int totalFlares = storedFlares + DRGFlaresConfig.GENERAL.flareReplenishQuantity.get();

                    flareCap.setStoredFlares(totalFlares);
                    PacketHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.player), new FlareCountSyncPacket(totalFlares));
                    flareCap.setReplenishTickCounter(0);
                }
                flareCap.incrementReplenishTickCounter();

                if (flareCap.getFlareThrowCoolDown() > 0)
                {
                    flareCap.decrementFlareThrowCoolDown();
                }
            });
    }

    @SubscribeEvent
    public static void playerJoin(EntityJoinWorldEvent event)
    {
        // need this to sync flare data on join or the client is stupid and displays max.
        if (!event.getWorld().isClientSide && event.getEntity() instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            player.getCapability(FlareDataCap.FLARE_DATA).ifPresent(flareCap -> {
                int storedFlares = flareCap.getStoredFlares();
                PacketHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new FlareCountSyncPacket(storedFlares));
            });
        }
    }

    /**
     * Avoids IDE warnings by returning null for fields that are injected in by forge.
     *
     * @return Not null!
     */
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T> T notNull()
    {
        return null;
    }

    public DRGFlares()
    {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DRGFlaresConfig.spec);

        // Register the few bits of start up stuff.
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Deferreds good.
        BlockRegistry.BLOCK_DEFERRED.register(modEventBus);
        BlockRegistry.TILE_ENTITY_DEFERRED.register(modEventBus);
        EntityRegistry.ENTITY_DEFERRED.register(modEventBus);

        PacketHandler.init();
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // Register flare cap so it exists.
        CapabilityManager.INSTANCE.register(FlareData.class, new NoopStorage<>(), () -> {
            throw new UnsupportedOperationException("Creating default instances is not supported. Why would you ever do this");
        });
    }

}
