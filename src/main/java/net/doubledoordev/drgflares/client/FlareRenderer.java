package net.doubledoordev.drgflares.client;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.doubledoordev.drgflares.DRGFlares;
import net.doubledoordev.drgflares.entity.FlareEntity;

public class FlareRenderer extends EntityRenderer<FlareEntity>
{
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(DRGFlares.MODID + ":textures/entity/flare/flare.png");
    FlareModel model = new FlareModel();
    Vector3d previousPosition = Vector3d.ZERO;

    public FlareRenderer(EntityRendererManager rendererManager)
    {
        super(rendererManager);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(FlareEntity entity, float p_225623_2_, float p_225623_3_, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int p_225623_6_)
    {
        matrixStack.pushPose();

        if (previousPosition != Vector3d.ZERO)
        {
            Vector3d movementMultiplier = previousPosition.subtract(entity.position());

            float rotationDampening = 15f;
            float xMovement = MathHelper.sin((float) movementMultiplier.x() / rotationDampening) * 360.0F;
            float yMovement = MathHelper.cos((float) movementMultiplier.y() / rotationDampening) * 360.0F;
            float zMovement = MathHelper.sin((float) movementMultiplier.z() / rotationDampening) * 360.0F;

            if (movementMultiplier != Vector3d.ZERO)
                mulPoseThrown(matrixStack, movementMultiplier, xMovement, yMovement, zMovement);
        }
        previousPosition = entity.position();

        IVertexBuilder flareVertexBuilder = renderTypeBuffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(matrixStack, flareVertexBuilder, p_225623_6_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        matrixStack.popPose();
        super.render(entity, p_225623_2_, p_225623_3_, matrixStack, renderTypeBuffer, p_225623_6_);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(FlareEntity entity)
    {
        return TEXTURE_LOCATION;
    }

    private void mulPoseThrown(MatrixStack matrixStack, Vector3d movementMultiplier, float xMovement, float yMovement, float zMovement)
    {
        matrixStack.mulPose((movementMultiplier.x() > 0) ? Vector3f.XP.rotationDegrees(xMovement) : Vector3f.XN.rotationDegrees(-xMovement));
        matrixStack.mulPose((movementMultiplier.y() > 0) ? Vector3f.YP.rotationDegrees(yMovement) : Vector3f.YN.rotationDegrees(-yMovement));
        matrixStack.mulPose((movementMultiplier.z() > 0) ? Vector3f.ZP.rotationDegrees(zMovement) : Vector3f.ZN.rotationDegrees(-zMovement));
    }
}
