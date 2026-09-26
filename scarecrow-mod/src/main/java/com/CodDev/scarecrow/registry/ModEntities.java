package com.CodDev.scarecrow.registry;

import com.CodDev.scarecrow.ScarecrowMod;
import com.CodDev.scarecrow.entity.ScarecrowEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ScarecrowMod.MOD_ID);

    public static final RegistryObject<EntityType<ScarecrowEntity>> SCARECROW = ENTITY_TYPES.register(
            "scarecrow",
            () -> EntityType.Builder.of(ScarecrowEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 2.3F)
                    .clientTrackingRange(48)
                    .updateInterval(3)
                    .fireImmune()
                    .build("scarecrow")
    );
}
