package com.CodDev.scarecrow.command;

import com.CodDev.scarecrow.entity.ScarecrowEntity;
import com.CodDev.scarecrow.entity.ScarecrowMode;
import com.CodDev.scarecrow.registry.ModEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ScarecrowCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("scarecrow")
                        .requires(source -> source.hasPermission(2))
                        .then(literal("pose1")
                                .executes(ctx -> spawnPose(ctx, ScarecrowMode.POSE_1))
                                .then(argument("pos", Vec3Argument.vec3())
                                        .executes(ctx -> spawnPoseAt(ctx, ScarecrowMode.POSE_1))))
                        .then(literal("pose2")
                                .executes(ctx -> spawnPose(ctx, ScarecrowMode.POSE_2))
                                .then(argument("pos", Vec3Argument.vec3())
                                        .executes(ctx -> spawnPoseAt(ctx, ScarecrowMode.POSE_2))))
                        .then(literal("pose3")
                                .executes(ctx -> spawnPose(ctx, ScarecrowMode.POSE_3))
                                .then(argument("pos", Vec3Argument.vec3())
                                        .executes(ctx -> spawnPoseAt(ctx, ScarecrowMode.POSE_3))))
                        .then(literal("chase")
                                .executes(ScarecrowCommand::spawnDormant)
                                .then(argument("pos", Vec3Argument.vec3())
                                        .executes(ScarecrowCommand::spawnDormantAt)))
                        .then(literal("instant")
                                .executes(ScarecrowCommand::spawnInstant)
                                .then(argument("pos", Vec3Argument.vec3())
                                        .executes(ScarecrowCommand::spawnInstantAt)))
        );
    }

    private static int spawnPose(CommandContext<CommandSourceStack> ctx, ScarecrowMode mode) {
        CommandSourceStack source = ctx.getSource();
        return spawn(source, source.getPosition(), mode, null);
    }

    private static int spawnPoseAt(CommandContext<CommandSourceStack> ctx, ScarecrowMode mode) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
        return spawn(source, pos, mode, null);
    }

    private static int spawnDormant(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        return spawn(source, source.getPosition(), ScarecrowMode.DORMANT, null);
    }

    private static int spawnDormantAt(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
        return spawn(source, pos, ScarecrowMode.DORMANT, null);
    }

    private static int spawnInstant(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        return spawn(source, source.getPosition(), ScarecrowMode.HUNTING, findNearestPlayer(source, source.getPosition()));
    }

    private static int spawnInstantAt(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
        return spawn(source, pos, ScarecrowMode.HUNTING, findNearestPlayer(source, pos));
    }

    private static Player findNearestPlayer(CommandSourceStack source, Vec3 pos) {
        if (source.getEntity() instanceof Player player) {
            return player;
        }
        List<Player> players = source.getLevel().getEntitiesOfClass(
                Player.class,
                new AABB(pos.x - 64.0D, pos.y - 64.0D, pos.z - 64.0D, pos.x + 64.0D, pos.y + 64.0D, pos.z + 64.0D)
        );
        return players.isEmpty() ? null : players.get(0);
    }

    private static int spawn(CommandSourceStack source, Vec3 pos, ScarecrowMode mode, Player instantTarget) {
        ServerLevel level = source.getLevel();
        ScarecrowEntity entity = ModEntities.SCARECROW.get().create(level);
        if (entity == null) {
            source.sendFailure(Component.literal("Could not create scarecrow entity"));
            return 0;
        }
        entity.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null);
        entity.setMode(mode);
        if (mode == ScarecrowMode.HUNTING) {
            if (instantTarget != null) {
                entity.beginHunt(instantTarget);
            }
        }
        level.addFreshEntity(entity);
        source.sendSuccess(() -> Component.literal("Spawned scarecrow (" + mode.name().toLowerCase() + ")"), true);
        return 1;
    }
}
