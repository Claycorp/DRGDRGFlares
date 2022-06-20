package net.doubledoordev.drgflares.client;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.doubledoordev.drgflares.DRGFlares;
import net.doubledoordev.drgflares.DRGFlaresConfig;
import net.doubledoordev.drgflares.entity.FlareEntity;

import static net.doubledoordev.drgflares.entity.FlareEntity.*;

public class FlareRenderer extends EntityRenderer<FlareEntity>
{
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(DRGFlares.MODID + ":textures/entity/flare/flare.png");
    FlareModel<FlareEntity> model;

    public FlareRenderer(EntityRendererProvider.Context rendererManager)
    {
        super(rendererManager);
        model = new FlareModel<>(rendererManager.bakeLayer(FlareModel.LAYER_LOCATION));
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(FlareEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        poseStack.pushPose();
        // Moved the following block of entity rotation setting here to use partial ticks to smooth out the rotations. MUCH BETTER.
        //Can't get the movement force if there's no previous position to work from or if they are the same.
        if (!entity.previousPosition.equals(Vec3.ZERO) || !entity.previousPosition.equals(entity.position()))
        {
            //Find out what the "force" behind the object is by subtracting the two positions.
            Vec3 movementForce = entity.previousPosition.subtract(entity.position());
            //Only rotate if there's force.
            if (!movementForce.equals(Vec3.ZERO))
            {
                //Set the entity data so the rendering can rotate the object and then this data can be stored for later use to keep objects rotated correctly on reload.
                entity.getEntityData().set(X_PHY_ROT, (float) (entity.getEntityData().get(X_PHY_ROT) + partialTicks * movementForce.x));
                entity.getEntityData().set(Y_PHY_ROT, (float) (entity.getEntityData().get(Y_PHY_ROT) + partialTicks * movementForce.y));
                entity.getEntityData().set(Z_PHY_ROT, (float) (entity.getEntityData().get(Z_PHY_ROT) + partialTicks * movementForce.z));
            }
        }
        entity.previousPosition = entity.position();

        // Translate the render up so the rotation point is moved into where the physical center is.
        poseStack.translate(0, .2, 0);

        //Get the force from the entity.
        float physicsXForce = entity.getEntityData().get(X_PHY_ROT);
        float physicsYForce = entity.getEntityData().get(Y_PHY_ROT);
        float physicsZForce = entity.getEntityData().get(Z_PHY_ROT);

        //Do some maths that give us some nicer rotations.
        float rotationDampening = DRGFlaresConfig.GENERALCONFIG.flareRotationStrength.get().floatValue();
        float xMovement = Mth.sin(physicsXForce / rotationDampening) * 360.0F;
        float yMovement = Mth.cos(physicsYForce / rotationDampening) * 360.0F;
        float zMovement = Mth.sin(physicsZForce / rotationDampening) * 360.0F;

        //Apply the rotations based off the force input so things rotate the correct direction.
        poseStack.mulPose((physicsXForce > 0) ? Vector3f.XP.rotationDegrees(xMovement) : Vector3f.XN.rotationDegrees(-xMovement));
        poseStack.mulPose((physicsYForce > 0) ? Vector3f.YP.rotationDegrees(yMovement) : Vector3f.YN.rotationDegrees(-yMovement));
        poseStack.mulPose((physicsZForce > 0) ? Vector3f.ZP.rotationDegrees(zMovement) : Vector3f.ZN.rotationDegrees(-zMovement));

        // Translate the object back down to place the actual render to match what the physical is doing.
        poseStack.translate(0, -.2, 0);

        VertexConsumer flareVertexBuilder = bufferSource.getBuffer(this.model.renderType(this.getTextureLocation(entity)));

        int flareColor = entity.getEntityData().get(FlareEntity.COLOR);
        switch (flareColor)
        {
            //Color shifting like jeb sheep. Stole from sheep.
            case 2 ->
            {
                float red;
                float green;
                float blue;
                int i = entity.tickCount / 25 + entity.getId();
                int j = DyeColor.values().length;
                int k = i % j;
                int l = (i + 1) % j;
                float f3 = ((float) (entity.tickCount % 25) + partialTicks) / 25.0F;
                float[] afloat1 = Sheep.getColorArray(DyeColor.byId(k));
                float[] afloat2 = Sheep.getColorArray(DyeColor.byId(l));
                red = afloat1[0] * (1.0F - f3) + afloat2[0] * f3;
                green = afloat1[1] * (1.0F - f3) + afloat2[1] * f3;
                blue = afloat1[2] * (1.0F - f3) + afloat2[2] * f3;
                this.model.renderToBuffer(poseStack, flareVertexBuilder, packedLight, OverlayTexture.NO_OVERLAY,
                        red,
                        green,
                        blue,
                        1);
                break;
            }
            default -> this.model.renderToBuffer(poseStack, flareVertexBuilder, packedLight, OverlayTexture.NO_OVERLAY,
                    (flareColor >> 16 & 255) / 255.0F,
                    (flareColor >> 8 & 255) / 255.0F,
                    (flareColor & 255) / 255.0F,
                    1);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(FlareEntity entity)
    {
        return TEXTURE_LOCATION;
    }
}
