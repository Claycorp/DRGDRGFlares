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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.doubledoordev.drgflares.DRGFlares;
import net.doubledoordev.drgflares.DRGFlaresConfig;
import net.doubledoordev.drgflares.entity.FlareEntity;

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
    public void render(FlareEntity entity, float p_225623_2_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_225623_6_)
    {
        poseStack.pushPose();

        //Get the force from the entity.
        float physicsXForce = entity.getEntityData().get(FlareEntity.X_PHY_ROT);
        float physicsYForce = entity.getEntityData().get(FlareEntity.Y_PHY_ROT);
        float physicsZForce = entity.getEntityData().get(FlareEntity.Z_PHY_ROT);

        //Do some maths that give us some nicer rotations.
        float rotationDampening = DRGFlaresConfig.GENERALCONFIG.flareRotationStrength.get().floatValue();
        float xMovement = Mth.sin(physicsXForce / rotationDampening) * 360.0F;
        float yMovement = Mth.cos(physicsYForce / rotationDampening) * 360.0F;
        float zMovement = Mth.sin(physicsZForce / rotationDampening) * 360.0F;

        //Apply the rotations based off the force input so things rotate the correct direction.
        poseStack.mulPose((physicsXForce > 0) ? Vector3f.XP.rotationDegrees(xMovement) : Vector3f.XN.rotationDegrees(-xMovement));
        poseStack.mulPose((physicsYForce > 0) ? Vector3f.YP.rotationDegrees(yMovement) : Vector3f.YN.rotationDegrees(-yMovement));
        poseStack.mulPose((physicsZForce > 0) ? Vector3f.ZP.rotationDegrees(zMovement) : Vector3f.ZN.rotationDegrees(-zMovement));

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
                this.model.renderToBuffer(poseStack, flareVertexBuilder, p_225623_6_, OverlayTexture.NO_OVERLAY,
                        red,
                        green,
                        blue,
                        1);
                break;
            }
            default -> this.model.renderToBuffer(poseStack, flareVertexBuilder, p_225623_6_, OverlayTexture.NO_OVERLAY,
                    (flareColor >> 16 & 255) / 255.0F,
                    (flareColor >> 8 & 255) / 255.0F,
                    (flareColor & 255) / 255.0F,
                    1);
        }
        poseStack.popPose();
        super.render(entity, p_225623_2_, partialTicks, poseStack, bufferSource, p_225623_6_);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(FlareEntity entity)
    {
        return TEXTURE_LOCATION;
    }
}
