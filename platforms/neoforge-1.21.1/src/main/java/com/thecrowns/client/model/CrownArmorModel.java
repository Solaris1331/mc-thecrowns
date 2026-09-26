package com.thecrowns.client.model;

import com.thecrowns.TheCrownsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/** A blocky crown that sits above the head without a helmet shell or top cap. */
public final class CrownArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheCrownsMod.MOD_ID, "crown_armor"), "main");

    public CrownArmorModel(ModelPart root) { super(root); }

    public static CrownArmorModel<LivingEntity> get() {
        return new CrownArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER_LOCATION));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        CubeListBuilder crown = CubeListBuilder.create()
                // very thin open band: no top face covering the player's hair/head
                .texOffs(0, 0).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 1.0F, 1.0F, CubeDeformation.NONE)
                .texOffs(0, 0).addBox(-4.5F, -9.0F, 3.5F, 9.0F, 1.0F, 1.0F, CubeDeformation.NONE)
                .texOffs(0, 0).addBox(-4.5F, -9.0F, -3.5F, 1.0F, 1.0F, 7.0F, CubeDeformation.NONE)
                .texOffs(0, 0).addBox(3.5F, -9.0F, -3.5F, 1.0F, 1.0F, 7.0F, CubeDeformation.NONE)

                // front crown points: broad stepped silhouettes instead of thin antennae
                .texOffs(24, 0).addBox(-4.25F, -11.5F, -4.45F, 1.5F, 2.5F, 0.9F, CubeDeformation.NONE)
                .texOffs(24, 0).addBox(-2.75F, -12.5F, -4.45F, 1.5F, 3.5F, 0.9F, CubeDeformation.NONE)
                .texOffs(24, 0).addBox(-1.25F, -13.0F, -4.45F, 2.5F, 4.0F, 0.9F, CubeDeformation.NONE)
                .texOffs(24, 0).addBox(-0.75F, -14.5F, -4.45F, 1.5F, 1.5F, 0.9F, CubeDeformation.NONE)
                .texOffs(24, 0).addBox(1.25F, -12.5F, -4.45F, 1.5F, 3.5F, 0.9F, CubeDeformation.NONE)
                .texOffs(24, 0).addBox(2.75F, -11.5F, -4.45F, 1.5F, 2.5F, 0.9F, CubeDeformation.NONE)

                // rear points: symmetrical so no back-left portion disappears
                .texOffs(24, 0).addBox(-4.25F, -11.0F, 3.55F, 1.5F, 2.0F, 0.9F, CubeDeformation.NONE)
                .texOffs(24, 0).addBox(-1.25F, -12.0F, 3.55F, 2.5F, 3.0F, 0.9F, CubeDeformation.NONE)
                .texOffs(24, 0).addBox(2.75F, -11.0F, 3.55F, 1.5F, 2.0F, 0.9F, CubeDeformation.NONE)

                // side crests
                .texOffs(24, 0).addBox(-4.45F, -11.75F, -1.25F, 0.9F, 2.75F, 2.5F, CubeDeformation.NONE)
                .texOffs(24, 0).addBox(3.55F, -11.75F, -1.25F, 0.9F, 2.75F, 2.5F, CubeDeformation.NONE)

                // small front jewel, visually separating crown from a plain headband
                .texOffs(48, 0).addBox(-0.75F, -10.25F, -4.8F, 1.5F, 1.5F, 0.45F, CubeDeformation.NONE);

        root.addOrReplaceChild("head", crown, PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }
}
