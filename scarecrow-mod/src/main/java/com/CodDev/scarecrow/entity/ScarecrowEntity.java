package com.CodDev.scarecrow.entity;

import com.CodDev.scarecrow.entity.goal.ScarecrowBreakBlockGoal;
import com.CodDev.scarecrow.network.ChaseSoundPacket;
import com.CodDev.scarecrow.network.ModNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import net.minecraft.world.phys.Vec3;

public class ScarecrowEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Integer> MODE =
            SynchedEntityData.defineId(ScarecrowEntity.class, EntityDataSerializers.INT);

    protected static final RawAnimation POSE_1_IDLE_ANIM = RawAnimation.begin().thenLoop("pose_1_idle");
    protected static final RawAnimation POSE_1_ANIM = RawAnimation.begin().thenPlayAndHold("pose_1");
    protected static final RawAnimation POSE_2_ANIM = RawAnimation.begin().thenPlayAndHold("pose_2");
    protected static final RawAnimation POSE_3_ANIM = RawAnimation.begin().thenPlayAndHold("Pose_3");
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int soundCooldown;
    private int stuckTicks;
    private boolean chaseSoundActive;

    public ScarecrowEntity(EntityType<? extends ScarecrowEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.soundCooldown = 60;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MODE, ScarecrowMode.DORMANT.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ScarecrowBreakBlockGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
    }

    public ScarecrowMode getMode() {
        int id = this.entityData.get(MODE);
        ScarecrowMode[] values = ScarecrowMode.values();
        if (id < 0 || id >= values.length) {
            return ScarecrowMode.DORMANT;
        }
        return values[id];
    }

    public void setMode(ScarecrowMode mode) {
        this.entityData.set(MODE, mode.ordinal());
    }

    public int getStuckTicks() {
        return this.stuckTicks;
    }

    public void resetStuckTicks() {
        this.stuckTicks = 0;
    }

    public void beginHunt(LivingEntity target) {
        this.setMode(ScarecrowMode.HUNTING);
        this.setTarget(target);
        this.setPersistenceRequired();
        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 1.0F, 0.8F);
        this.broadcastChaseSound(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            serverTick();
        }
    }

    private void serverTick() {
        switch (this.getMode()) {
            case POSE_1:
            case POSE_2:
            case POSE_3:
                handleStaticPose();
                break;
            case DORMANT:
                handleDormant();
                break;
            case HUNTING:
                handleHunting();
                break;
            default:
                break;
        }
    }

    private void handleStaticPose() {
        if (this.tickCount == 1) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.VEX_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.7F);
        }
        if (getDirectWatcher(22.0D) != null) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.8F);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(), 14, 0.3D, 0.6D, 0.3D, 0.02D);
            this.discard();
        }
    }

    private void handleDormant() {
        if (this.soundCooldown-- <= 0) {
            this.soundCooldown = 100 + this.random.nextInt(140);
            this.level().playSound(null, this.blockPosition(), SoundEvents.VEX_AMBIENT, SoundSource.HOSTILE, 0.6F, 0.6F);
        }
        Player watcher = getDirectWatcher(28.0D);
        if (watcher != null) {
            beginHunt(watcher);
        }
    }

    private void handleHunting() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.setTarget(null);
        }
        this.setSprinting(true);
        double horizontalSpeedSq = this.getDeltaMovement().horizontalDistanceSqr();
        if (horizontalSpeedSq < 0.0025D && this.getTarget() != null) {
            this.stuckTicks++;
        } else {
            this.stuckTicks = 0;
        }
    }

    private Player getDirectWatcher(double maxDistance) {
        Vec3 centerPos = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(maxDistance))) {
            if (!player.isAlive() || player.isSpectator()) {
                continue;
            }
            Vec3 eyePos = player.getEyePosition(1.0F);
            if (eyePos.distanceTo(centerPos) > maxDistance) {
                continue;
            }
            Vec3 toEntity = centerPos.subtract(eyePos).normalize();
            Vec3 look = player.getLookAngle().normalize();
            double dot = toEntity.dot(look);
            if (dot < 0.975D) {
                continue;
            }
            if (!player.hasLineOfSight(this)) {
                continue;
            }
            return player;
        }
        return null;
    }

    private void broadcastChaseSound(boolean start) {
        this.chaseSoundActive = start;
        ModNetworking.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> this),
                new ChaseSoundPacket(this.getId(), start)
        );
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.chaseSoundActive) {
            ModNetworking.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new ChaseSoundPacket(this.getId(), true)
            );
        }
    }

    @Override
    public void die(DamageSource source) {
        if (this.chaseSoundActive) {
            broadcastChaseSound(false);
        }
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.chaseSoundActive) {
            broadcastChaseSound(false);
        }
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSq) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 6, this::poseController));
    }

    private <E extends ScarecrowEntity> PlayState poseController(AnimationState<E> state) {
        switch (this.getMode()) {
            case POSE_1:
                return state.setAndContinue(POSE_1_ANIM);
            case POSE_2:
                return state.setAndContinue(POSE_2_ANIM);
            case POSE_3:
                return state.setAndContinue(POSE_3_ANIM);
            case HUNTING:
                if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0025D) {
                    return state.setAndContinue(RUN_ANIM);
                }
                return state.setAndContinue(WALK_ANIM);
            case DORMANT:
            default:
                return state.setAndContinue(POSE_1_IDLE_ANIM);
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
