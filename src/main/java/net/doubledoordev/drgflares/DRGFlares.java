package net.doubledoordev.drgflares;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;

import net.doubledoordev.drgflares.block.BlockRegistry;
import net.doubledoordev.drgflares.capability.FlareData;
import net.doubledoordev.drgflares.capability.FlareDataCap;
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
        if (event.getObject() instanceof Player)
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
                int maxFlares = DRGFlaresConfig.GENERALCONFIG.flareQuantity.get();

                if (event.player.isCreative() || (event.player.isSpectator() && !DRGFlaresConfig.GENERALCONFIG.spectatorsRequiredToGenerateFlares.get()) && flareCap.getStoredFlares() != maxFlares)
                {
                    flareCap.setStoredFlares(maxFlares);
                    flareCap.setFlareThrowCoolDown(0);
                    flareCap.setReplenishTickCounter(0);
                    PacketHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.player), new FlareCountSyncPacket(maxFlares));
                    return;
                }

                if (flareCap.getReplenishTickCounter() >= DRGFlaresConfig.GENERALCONFIG.flareReplenishTime.get() && storedFlares < maxFlares)
                {
                    int totalFlares = storedFlares + DRGFlaresConfig.GENERALCONFIG.flareReplenishQuantity.get();

                    flareCap.setStoredFlares(totalFlares);
                    PacketHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.player), new FlareCountSyncPacket(totalFlares));
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
    public static void chatMessage(ServerChatEvent event)
    {
        String message = event.getMessage().getString().toLowerCase(Locale.ROOT);

        if (message.contains("setflarecolor"))
        {
            int color = stringToColorInt(message.substring(13));

            String messageToSend = switch (color)
                    {
                        case -1 -> "Set flare color to: Default color (#a66ff2)";
                        case -2 -> "Set flare color to: Fixed rainbow. (rainbow)";
                        case -3 -> "Set flare color to: Color shift (jeb)";
                        case -4 -> "Set flare color to: Random color (random)";
                        default -> "Set flare color to: " + Integer.toHexString(color);
                    };

            event.getPlayer().getCapability(FlareDataCap.FLARE_DATA).ifPresent(flareCap -> flareCap.setFlareColor(color));
            event.getPlayer().displayClientMessage(Component.literal(messageToSend), true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerJoin(EntityJoinLevelEvent event)
    {
        // need this to sync flare data on join or the client is stupid and displays max.
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Player player)
        {
            player.getCapability(FlareDataCap.FLARE_DATA).ifPresent(flareCap -> {
                int storedFlares = flareCap.getStoredFlares();
                PacketHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new FlareCountSyncPacket(storedFlares));
            });
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.register(FlareData.class);
    }

    public DRGFlares()
    {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DRGFlaresConfig.spec);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Deferred good.
        BlockRegistry.BLOCK_DEFERRED.register(modEventBus);
        BlockRegistry.TILE_ENTITY_DEFERRED.register(modEventBus);
        EntityRegistry.ENTITY_DEFERRED.register(modEventBus);

        PacketHandler.init();
    }

    public static int stringToColorInt(String colorString)
    {
        String preparedColorString = colorString.trim();
        int color = 10907634; //Some purple color default AKA #a66ff2.
        //decode operation requires the # symbol to decode it.
        if (preparedColorString.startsWith("#"))
        {
            try
            {
                color = Integer.decode(preparedColorString);
            }
            catch (NumberFormatException e)
            {
                LOGGER.error("INCORRECT FORMAT FOR FLARE COLOR USING DEFAULT! Color provided: " + preparedColorString);
            }
            return color;
        }
        //If no #, try our custom render options.
        else
        {
            switch (preparedColorString)
            {
                //Numbers are arbitrary but must match what is used in the render method with special renders. Otherwise, a valid color number can be set.
                case "rainbow", "-2":
                    return -2;
                case "jeb", "-3":
                    return -3;
                //Handled in packet toss as the ID needs to be random per flare not per render frame.
                case "random", "-4":
                    return -4;
            }
        }

        LOGGER.error("INCORRECT FORMAT FOR FLARE COLOR USING DEFAULT! Color provided: " + preparedColorString);
        return color;
    }

    /**
     * Converts the components of a color, as specified by the HSB
     * model, to an equivalent set of values for the default RGB model.
     * <p>
     * The <code>saturation</code> and <code>brightness</code> components
     * should be floating-point values between zero and one
     * (numbers in the range 0.0-1.0).  The <code>hue</code> component
     * can be any floating-point number.  The floor of this number is
     * subtracted from it to create a fraction between 0 and 1.  This
     * fractional number is then multiplied by 360 to produce the hue
     * angle in the HSB color model.
     * <p>
     * The integer that is returned by <code>HSBtoRGB</code> encodes the
     * value of a color in bits 0-23 of an integer value that is the same
     * format used by the method .
     * This integer can be supplied as an argument to the
     * <code>Color</code> constructor that takes a single integer argument.
     *
     * @param hue        the hue component of the color
     * @param saturation the saturation of the color
     * @param brightness the brightness of the color
     * @return the RGB value of the color with the indicated hue,
     * saturation, and brightness.
     **/
    //Borrowed from AWT as it's not found on servers and thus would crash.
    public static int HSBtoRGB(float hue, float saturation, float brightness)
    {
        int r = 0, g = 0, b = 0;
        if (saturation == 0)
        {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        }
        else
        {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h)
            {
                case 0 ->
                {
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                }
                case 1 ->
                {
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                }
                case 2 ->
                {
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                }
                case 3 ->
                {
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                }
                case 4 ->
                {
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                }
                case 5 ->
                {
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                }
            }
        }
        return 0xff000000 | (r << 16) | (g << 8) | (b);
    }
}
