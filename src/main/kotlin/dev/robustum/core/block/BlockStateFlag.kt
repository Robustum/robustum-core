package dev.robustum.core.block

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ModifiableWorld

fun ModifiableWorld.setBlockState(pos: BlockPos, state: BlockState, vararg flags: BlockStateFlag): Boolean =
    setBlockState(this, pos, state, flags.toSet())

private fun setBlockState(
    world: ModifiableWorld,
    pos: BlockPos,
    state: BlockState,
    flags: Set<BlockStateFlag>
): Boolean = world.setBlockState(pos, state, flags.map { it.value }.sum())

enum class BlockStateFlag(val value: Byte) {
    PROPAGATE_CHANGE(0b1),
    NOTIFY_LISTENERS(0b10),
    NO_REDRAW(0b100),
    REDRAW_ON_MAIN_THREAD(0b1000),
    FORCE_STATE(0b10000),
    SKIP_DROPS(0b100000),
    MOVED(0b1000000),
    ;
}