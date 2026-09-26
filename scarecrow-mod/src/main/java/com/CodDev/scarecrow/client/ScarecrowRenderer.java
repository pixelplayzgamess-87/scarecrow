package com.CodDev.scarecrow.client;

import com.CodDev.scarecrow.entity.ScarecrowEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ScarecrowRenderer extends GeoEntityRenderer<ScarecrowEntity> {

    public ScarecrowRenderer(EntityRendererProvider.Context context) {
        super(context, new ScarecrowModel());
        this.shadowRadius = 0.5F;
    }
}
