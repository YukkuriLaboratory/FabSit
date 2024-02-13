package test.entity

import extension.addInstantFinalTask
import extension.runCatchingAssertion
import extension.waitAndRun
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import mock.createMockServerPlayer
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.fill1890.fabsit.entity.Pose
import net.fill1890.fabsit.error.PoseException.MidairException
import net.minecraft.block.Blocks
import net.minecraft.block.SlabBlock
import net.minecraft.block.enums.SlabType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.test.GameTest
import net.minecraft.test.TestContext
import net.minecraft.util.math.BlockPos
import net.yukulab.fabsit.DelegatedLogger
import net.yukulab.fabsit.extension.pose

@Suppress("UNUSED")
class TestPoseManagerEntity : FabricGameTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun checkSitOnSlabBlock(context: TestContext) = runCatchingAssertion(logger) {
        poseOnSlabBlock(context, Pose.SITTING)
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun checkSitOnSlabBlockLowHeight(context: TestContext) = runCatchingAssertion(logger) {
        poseOnSlabBlock(context, Pose.SITTING, blockHeight = 2)
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun checkSitOnSlabBlockWithSneak(context: TestContext) = runCatchingAssertion(logger) {
        poseOnSlabBlock(context, Pose.SITTING, removeWithSneak = true)
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun checkLayOnSlabBlock(context: TestContext) = runCatchingAssertion(logger) {
        poseOnSlabBlock(context, Pose.LAYING)
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun checkLayOnSlabBlockLowHeight(context: TestContext) = runCatchingAssertion(logger) {
        poseOnSlabBlock(context, Pose.SITTING, blockHeight = 2)
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun checkLayOnSlabBlockWithSneak(context: TestContext) = runCatchingAssertion(logger) {
        poseOnSlabBlock(context, Pose.SITTING, removeWithSneak = true)
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun checkSpinOnSlabBlock(context: TestContext) = runCatchingAssertion(logger) {
        poseOnSlabBlock(context, Pose.SPINNING)
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun checkSpinOnSlabBlockLowHeight(context: TestContext) = runCatchingAssertion(logger) {
        poseOnSlabBlock(context, Pose.SITTING, blockHeight = 2)
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun checkSpinOnSlabBlockWithSneak(context: TestContext) = runCatchingAssertion(logger) {
        poseOnSlabBlock(context, Pose.SITTING, removeWithSneak = true)
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun checkAirBockShouldFailed(context: TestContext) = runCatchingAssertion(logger) {
        context.addInstantFinalTask(logger) {
            val mockPlayer = context.createMockServerPlayer(BlockPos(0, 3, 0))
            mockPlayer.pose(Pose.SITTING).shouldBeFailure<MidairException>()
            checkPlayerNotPosing(mockPlayer)
        }
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun checkBlockUpdated(context: TestContext) = runCatchingAssertion(logger) {
        val blockHeight = 4
        context.setBlockState(
            BlockPos(0, blockHeight, 0),
            Blocks.STONE_SLAB.defaultState.with(SlabBlock.TYPE, SlabType.BOTTOM),
        )
        val mockPlayer = context.createMockServerPlayer(BlockPos(0, blockHeight + 1, 0))
        mockPlayer.updatePosition(mockPlayer.x, mockPlayer.y - 0.5, mockPlayer.z)
        val pose = Pose.SITTING
        mockPlayer.pose(pose).shouldBeSuccess()
        context.waitAndRun(2, logger) {
            checkPlayerPosing(mockPlayer, pose)
            context.setBlockState(BlockPos(0, blockHeight, 0), Blocks.AIR)
            context.waitAndRun(2) {
                context.addInstantFinalTask(logger) {
                    checkPlayerNotPosing(mockPlayer)
                }
            }
        }
    }

    private fun poseOnSlabBlock(context: TestContext, pose: Pose, blockHeight: Int = 4, removeWithSneak: Boolean = false) {
        context.setBlockState(
            BlockPos(0, blockHeight, 0),
            Blocks.STONE_SLAB.defaultState.with(SlabBlock.TYPE, SlabType.BOTTOM),
        )
        val mockPlayer = context.createMockServerPlayer(BlockPos(0, blockHeight + 1, 0))
        mockPlayer.updatePosition(mockPlayer.x, mockPlayer.y - 0.4, mockPlayer.z)
        mockPlayer.pose(pose).shouldBeSuccess()
        context.waitAndRun(2, logger) {
            checkPlayerPosing(mockPlayer, pose)
            if (removeWithSneak) {
                mockPlayer.isSneaking = true
            } else {
                mockPlayer.pose(pose, checkSpam = false).shouldBeSuccess()
            }
            context.waitAndRun(2) {
                context.addInstantFinalTask(logger) {
                    checkPlayerNotPosing(mockPlayer)
                }
            }
        }
    }

    private fun checkPlayerPosing(player: ServerPlayerEntity, pose: Pose) {
        player.vehicle.shouldNotBeNull().isAlive.shouldBeTrue()
        if (pose in setOf(Pose.LAYING, Pose.SPINNING)) {
            player.isInvisible.shouldBeTrue()
        } else {
            player.isInvisible.shouldBeFalse()
        }
    }

    private fun checkPlayerNotPosing(player: ServerPlayerEntity) {
        player.vehicle.shouldBeNull()
        player.isInvisible.shouldBeFalse()
    }

    companion object {
        val logger by DelegatedLogger()
    }
}
