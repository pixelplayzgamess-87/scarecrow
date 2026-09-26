package com.CodDev.scarecrow.registry;

import com.CodDev.scarecrow.ScarecrowMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ScarecrowMod.MOD_ID);

    public static final RegistryObject<SoundEvent> CHASE = SOUNDS.register(
            "chase",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ScarecrowMod.MOD_ID, "chase"))
    );
}
