package com.CodDev.scarecrow;

import com.CodDev.scarecrow.command.ScarecrowCommand;
import com.CodDev.scarecrow.network.ModNetworking;
import com.CodDev.scarecrow.registry.ModEntities;
import com.CodDev.scarecrow.registry.ModSounds;
import com.CodDev.scarecrow.entity.ScarecrowEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

@Mod(ScarecrowMod.MOD_ID)
public class ScarecrowMod {

    public static final String MOD_ID = "scarecrow";

    public ScarecrowMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        GeckoLib.initialize();
        ModEntities.ENTITY_TYPES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::onAttributeCreate);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetworking::register);
    }

    private void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SCARECROW.get(), ScarecrowEntity.createAttributes().build());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ScarecrowCommand.register(event.getDispatcher());
    }
}
