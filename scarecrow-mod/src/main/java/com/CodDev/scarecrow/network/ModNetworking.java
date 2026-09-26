package com.CodDev.scarecrow.network;

import com.CodDev.scarecrow.ScarecrowMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ScarecrowMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                ChaseSoundPacket.class,
                ChaseSoundPacket::encode,
                ChaseSoundPacket::decode,
                ChaseSoundPacket::handle
        );
    }
}
