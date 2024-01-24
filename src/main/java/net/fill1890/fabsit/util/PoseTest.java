package net.fill1890.fabsit.util;

import net.fill1890.fabsit.config.Config;
import net.fill1890.fabsit.config.ConfigManager;
import net.fill1890.fabsit.entity.Pose;
import net.fill1890.fabsit.error.PoseException;
import net.fill1890.fabsit.extension.ForceSwimFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

// maybe rename this later
public class PoseTest {
    /**
     * Check if a player can currently perform a given pose
     *
     * On a successful return, pose is valid
     * If pose is invalid, will send the relevant message to the player and throw an exception
     *
     * @param player player to check posing for
     * @throws PoseException if pose is not valid
     */
    public static void confirmPosable(ServerPlayerEntity player, BlockPos target) throws PoseException {
        // check if spectating
        if(player.isSpectator())
            throw new PoseException.SpectatorException();

        var forceSwim = ((ForceSwimFlag) player).fabSit$shouldForceSwim();
        // check if underwater
        if ((!forceSwim && player.isInsideWaterOrBubbleColumn()) && !ConfigManager.getConfig().allow_posing_underwater)
            throw new PoseException.StateException();

        // check if flying, jumping, swimming, sleeping, or underwater
        if(
                player.isFallFlying() || player.getVelocity().y > 0
                        || (!forceSwim && player.isSwimming())
                || player.isSleeping())
            throw new PoseException.StateException();

        var blockPos = BlockPos.ofFloored(player.getPos());
        // If player standing on a bottom half slab, playerPos equals blockPos
        var below = player.getEntityWorld().getBlockState(blockPos);
        if (below.isAir()) {
            below = player.getEntityWorld().getBlockState(blockPos.down());
        }

        // check if in midair
        if(below.isAir() && !ConfigManager.getConfig().allow_posing_midair)
            throw new PoseException.MidairException();

        if(
                (ConfigManager.getConfig().centre_on_blocks || ConfigManager.getConfig().right_click_sit)
                && ConfigManager.occupiedBlocks.contains(target)
        ) {
            throw new PoseException.BlockOccupied();
        }
    }

    public static void confirmEnabled(Pose pose) throws PoseException {
        Config.Poses poses = ConfigManager.getConfig().allow_poses;
        boolean allowed = switch (pose) {
            case LAYING -> poses.lay;
            case SPINNING -> poses.spin;
            case SITTING -> poses.sit;
            case SWIMMING -> poses.swim;
        };

        if(!allowed) throw new PoseException.PoseDisabled();
    }
}
