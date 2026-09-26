package com.CodDev.scarecrow.entity.goal;

import com.CodDev.scarecrow.entity.ScarecrowEntity;
import com.CodDev.scarecrow.entity.ScarecrowMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ScarecrowBreakBlockGoal extends Goal {

    private final ScarecrowEntity scarecrow;
    private BlockPos targetBlock;
    private int breakProgress;
    private int breakDuration;

    public ScarecrowBreakBlockGoal(ScarecrowEntity scarecrow) {
        this.scarecrow = scarecrow;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.scarecrow.getMode() != ScarecrowMode.HUNTING) {
            return false;
        }
        if (this.scarecrow.getTarget() == null) {
            return false;
        }
        if (!this.scarecrow.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }
        return this.scarecrow.getStuckTicks() > 12 && findBreakableBlock() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetBlock != null
                && this.scarecrow.getMode() == ScarecrowMode.HUNTING
                && this.scarecrow.getTarget() != null
                && !this.scarecrow.level().getBlockState(this.targetBlock).isAir();
    }

    @Override
    public void start() {
        this.targetBlock = findBreakableBlock();
        this.breakProgress = 0;
        this.breakDuration = 30;
        this.scarecrow.resetStuckTicks();
    }

    @Override
    public void stop() {
        if (this.targetBlock != null) {
            this.scarecrow.level().destroyBlockProgress(this.scarecrow.getId(), this.targetBlock, -1);
        }
        this.targetBlock = null;
        this.breakProgress = 0;
    }

    @Override
    public void tick() {
        if (this.targetBlock == null) {
            this.targetBlock = findBreakableBlock();
            if (this.targetBlock == null) {
                return;
            }
        }
        this.scarecrow.getLookControl().setLookAt(
                this.targetBlock.getX() + 0.5D,
                this.targetBlock.getY() + 0.5D,
                this.targetBlock.getZ() + 0.5D
        );
        this.breakProgress++;
        int stage = Math.min(9, (int) (((float) this.breakProgress / (float) this.breakDuration) * 10.0F));
        Level level = this.scarecrow.level();
        level.destroyBlockProgress(this.scarecrow.getId(), this.targetBlock, stage);
        if (this.breakProgress >= this.breakDuration) {
            level.destroyBlock(this.targetBlock, false, this.scarecrow);
            level.destroyBlockProgress(this.scarecrow.getId(), this.targetBlock, -1);
            this.targetBlock = null;
            this.breakProgress = 0;
            this.scarecrow.resetStuckTicks();
        }
    }

    private BlockPos findBreakableBlock() {
        Vec3 look = this.scarecrow.getLookAngle();
        BlockPos origin = this.scarecrow.blockPosition();
        Direction facing = Direction.getNearest((float) look.x, (float) look.y, (float) look.z);
        for (int distance = 1; distance <= 2; distance++) {
            for (int yOffset = 0; yOffset <= 1; yOffset++) {
                BlockPos pos = origin.relative(facing, distance).above(yOffset);
                BlockState state = this.scarecrow.level().getBlockState(pos);
                if (state.isAir()) {
                    continue;
                }
                float hardness = state.getDestroySpeed(this.scarecrow.level(), pos);
                if (hardness < 0.0F || hardness > 50.0F) {
                    continue;
                }
                if (this.scarecrow.level().getBlockEntity(pos) != null) {
                    continue;
                }
                return pos;
            }
        }
        return null;
    }
}
