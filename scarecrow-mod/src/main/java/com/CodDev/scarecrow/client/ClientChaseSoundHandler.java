package com.CodDev.scarecrow.client;

import com.CodDev.scarecrow.client.sound.ChaseSoundInstance;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class ClientChaseSoundHandler {

    private static final Map<Integer, ChaseSoundInstance> ACTIVE = new HashMap<>();

    public static void handle(int entityId, boolean start) {
        Minecraft minecraft = Minecraft.getInstance();
        if (start) {
            if (ACTIVE.containsKey(entityId)) {
                return;
            }
            ChaseSoundInstance instance = new ChaseSoundInstance(entityId);
            ACTIVE.put(entityId, instance);
            minecraft.getSoundManager().play(instance);
        } else {
            ChaseSoundInstance instance = ACTIVE.remove(entityId);
            if (instance != null) {
                minecraft.getSoundManager().stop(instance);
            }
        }
    }
}
