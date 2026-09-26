package com.CodDev.scarecrow.network;

import com.CodDev.scarecrow.client.ClientChaseSoundHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChaseSoundPacket {

    private final int entityId;
    private final boolean start;

    public ChaseSoundPacket(int entityId, boolean start) {
        this.entityId = entityId;
        this.start = start;
    }

    public static void encode(ChaseSoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeBoolean(packet.start);
    }

    public static ChaseSoundPacket decode(FriendlyByteBuf buffer) {
        return new ChaseSoundPacket(buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(ChaseSoundPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientChaseSoundHandler.handle(packet.entityId, packet.start)));
        context.setPacketHandled(true);
    }
}
