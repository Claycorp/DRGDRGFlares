package net.doubledoordev.drgflares.client;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.doubledoordev.drgflares.entity.FlareEntity;

public class FlareModel extends EntityModel<FlareEntity>
{
    private final ModelRenderer topRing;
    private final ModelRenderer bottomRing;
    private final ModelRenderer core;

    public FlareModel()
    {
        texWidth = 32;
        texHeight = 32;

        topRing = new ModelRenderer(this);
        topRing.setPos(0.0F, 0.0F, 0.0F);
        topRing.texOffs(0, 13).addBox(-3.0F, 9.0F, -3.0F, 2.0F, 3.0F, 2.0F, 0.0F, false);
        topRing.texOffs(0, 13).addBox(1.0F, 9.0F, -3.0F, 2.0F, 3.0F, 2.0F, 0.0F, false);
        topRing.texOffs(0, 13).addBox(1.0F, 9.0F, 1.0F, 2.0F, 3.0F, 2.0F, 0.0F, false);
        topRing.texOffs(0, 13).addBox(-3.0F, 9.0F, 1.0F, 2.0F, 3.0F, 2.0F, 0.0F, false);
        topRing.texOffs(13, 15).addBox(-1.0F, 10.0F, -2.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        topRing.texOffs(8, 13).addBox(-2.5F, 10.0F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
        topRing.texOffs(12, 13).addBox(-1.0F, 10.0F, 1.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        topRing.texOffs(12, 0).addBox(1.5F, 10.0F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);

        bottomRing = new ModelRenderer(this);
        bottomRing.setPos(0.0F, 0.0F, 0.0F);
        bottomRing.texOffs(0, 13).addBox(-3.0F, -1.0F, -3.0F, 2.0F, 3.0F, 2.0F, 0.0F, false);
        bottomRing.texOffs(0, 13).addBox(1.0F, -1.0F, -3.0F, 2.0F, 3.0F, 2.0F, 0.0F, false);
        bottomRing.texOffs(0, 13).addBox(1.0F, -1.0F, 1.0F, 2.0F, 3.0F, 2.0F, 0.0F, false);
        bottomRing.texOffs(0, 13).addBox(-3.0F, -1.0F, 1.0F, 2.0F, 3.0F, 2.0F, 0.0F, false);
        bottomRing.texOffs(13, 15).addBox(-1.0F, 0.0F, -2.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        bottomRing.texOffs(8, 13).addBox(-2.5F, 0.0F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
        bottomRing.texOffs(12, 13).addBox(-1.0F, 0.0F, 1.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        bottomRing.texOffs(12, 0).addBox(1.5F, 0.0F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);

        core = new ModelRenderer(this);
        core.setPos(0.0F, 1.0F, 0.0F);
        core.texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 9.0F, 4.0F, 0.0F, false);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void setupAnim(FlareEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        //previously the render function, render code was moved to a method below
    }

    @ParametersAreNonnullByDefault
    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        topRing.render(matrixStack, buffer, packedLight, packedOverlay);
        bottomRing.render(matrixStack, buffer, packedLight, packedOverlay);
        core.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
