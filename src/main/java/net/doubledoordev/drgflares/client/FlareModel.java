package net.doubledoordev.drgflares.client;
// Made with Blockbench 4.2.4
// Exported for Minecraft version 1.17 - 1.18 with Mojang mappings
// Paste this class into your mod and generate all required imports

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.doubledoordev.drgflares.DRGFlares;

public class FlareModel<T extends Entity> extends EntityModel<T>
{
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DRGFlares.MODID, "flaremodel"), "main");

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition topRing = partdefinition.addOrReplaceChild("topRing", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, 9.0F, -3.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(1.0F, 9.0F, -3.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(1.0F, 9.0F, 1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-3.0F, 9.0F, 1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(13, 15).addBox(-1.0F, 10.0F, -2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 13).addBox(-2.5F, 10.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(12, 13).addBox(-1.0F, 10.0F, 1.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 0).addBox(1.5F, 10.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bottomRing = partdefinition.addOrReplaceChild("bottomRing", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -1.0F, -3.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(1.0F, -1.0F, -3.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(1.0F, -1.0F, 1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-3.0F, -1.0F, 1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(13, 15).addBox(-1.0F, 0.0F, -2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 13).addBox(-2.5F, 0.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(12, 13).addBox(-1.0F, 0.0F, 1.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 0).addBox(1.5F, 0.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition core = partdefinition.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    private final ModelPart topRing;
    private final ModelPart bottomRing;
    private final ModelPart core;

    public FlareModel(ModelPart root)
    {
        this.topRing = root.getChild("topRing");
        this.bottomRing = root.getChild("bottomRing");
        this.core = root.getChild("core");
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {

    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
    {
        topRing.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        bottomRing.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        core.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}