package net.doubledoordev.drgflares;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import net.doubledoordev.drgflares.block.BlockRegistry;
import net.doubledoordev.drgflares.capability.FlareDataCap;
import net.doubledoordev.drgflares.client.FlareRenderer;
import net.doubledoordev.drgflares.entity.EntityRegistry;
import net.doubledoordev.drgflares.networking.PacketHandler;
import net.doubledoordev.drgflares.networking.ThrowFlarePacket;


public class ClientEventHandler
{
    // Keybind for Flares
    public static final KeyBinding THROW_FLARE = new KeyBinding("drgflares.key.throwflare", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_V, "Flares");

    public static PlayerEntity getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class Mod
    {

        @SubscribeEvent
        public static void clientRendering(FMLClientSetupEvent event)
        {
            ClientRegistry.registerKeyBinding(THROW_FLARE);
            RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.FLARE_ENTITY.get(), manager -> new FlareRenderer(event.getMinecraftSupplier().get().getEntityRenderDispatcher()));
            DeferredWorkQueue.runLater(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.FAKE_LIGHT.get(), RenderType.cutout()));
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
            PlayerEntity player = Minecraft.getInstance().player;

            if (DRGFlaresConfig.GENERAL.displayFlareCount.get() && player != null)
            {
                player.getCapability(FlareDataCap.FLARE_DATA).ifPresent(flareCap -> {
                    event.getLeft().add("Flares: " + flareCap.getStoredFlares() + "/" + DRGFlaresConfig.GENERAL.flareQuantity.get());
                });
            }
        }
    }
}
