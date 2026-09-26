package com.CodDev.scarecrow.client.sound;

import com.CodDev.scarecrow.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ChaseSoundInstance extends AbstractTickableSoundInstance {

    private final int entityId;

    public ChaseSoundInstance(int entityId) {
        super(ModSounds.CHASE.get(), SoundSource.HOSTILE, RandomSource.create());
        this.entityId = entityId;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.relative = false;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        Entity entity = resolveEntity();
        if (entity != null) {
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
        }
    }

    private Entity resolveEntity() {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        return level.getEntity(this.entityId);
    }

    @Override
    public void tick() {
        Entity entity = resolveEntity();
        if (entity == null || entity.isRemoved()) {
            this.stopped = true;
            return;
        }
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }
}
