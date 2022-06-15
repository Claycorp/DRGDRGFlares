package net.doubledoordev.drgflares;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

import com.mojang.blaze3d.platform.InputConstants;
import net.doubledoordev.drgflares.capability.FlareDataCap;
import net.doubledoordev.drgflares.client.FlareModel;
import net.doubledoordev.drgflares.client.FlareRenderer;
import net.doubledoordev.drgflares.entity.EntityRegistry;
import net.doubledoordev.drgflares.networking.PacketHandler;
import net.doubledoordev.drgflares.networking.ThrowFlarePacket;


public class ClientEventHandler
{
    // Keybind for Flares
    public static final KeyMapping THROW_FLARE = new KeyMapping("drgflares.key.throwflare", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "drgflares.menu.key.category.throwFlare");

    public static Player getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class Mod
    {
        @SubscribeEvent
        public static void clientRendering(EntityRenderersEvent.RegisterRenderers event)
        {
            ClientRegistry.registerKeyBinding(THROW_FLARE);

            event.registerEntityRenderer(EntityRegistry.FLARE_ENTITY.get(), FlareRenderer::new);
        }

        @SubscribeEvent
        public static void clientLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
        {
            event.registerLayerDefinition(FlareModel.LAYER_LOCATION, FlareModel::createBodyLayer);
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
    public static class Forge
    {
        // Catch our key press, fire off a packet to the server for handling.
        @SubscribeEvent
        public static void onKeyEvent(InputEvent.KeyInputEvent event)
        {
            if (THROW_FLARE.isDown())
            {
                PacketHandler.send(PacketDistributor.SERVER.noArg(), new ThrowFlarePacket());
            }
        }

        @SubscribeEvent
        public static void drawTextEvent(RenderGameOverlayEvent.Text event)
        {
            Player player = Minecraft.getInstance().player;

            if (DRGFlaresConfig.GENERALCONFIG.displayFlareCount.get() && player != null)
            {
                player.getCapability(FlareDataCap.FLARE_DATA).ifPresent(flareCap -> event.getLeft().add("Flares: " + flareCap.getStoredFlares() + "/" + DRGFlaresConfig.GENERALCONFIG.flareQuantity.get()));
            }
        }
    }
}
