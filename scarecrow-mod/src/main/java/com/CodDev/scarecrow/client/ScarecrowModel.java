package com.CodDev.scarecrow.client;

import com.CodDev.scarecrow.ScarecrowMod;
import com.CodDev.scarecrow.entity.ScarecrowEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ScarecrowModel extends GeoModel<ScarecrowEntity> {

    @Override
    public ResourceLocation getModelResource(ScarecrowEntity animatable) {
        return new ResourceLocation(ScarecrowMod.MOD_ID, "geo/scarecrow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ScarecrowEntity animatable) {
        return new ResourceLocation(ScarecrowMod.MOD_ID, "textures/entity/scarecrow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ScarecrowEntity animatable) {
        return new ResourceLocation(ScarecrowMod.MOD_ID, "animations/scarecrow.animation.json");
    }
}
