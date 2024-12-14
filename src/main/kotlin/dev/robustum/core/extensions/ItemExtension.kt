package dev.robustum.core.extensions

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext

//    ItemStack    //

/**
 * 指定された[item]を持っている場合はtrueを，それ以外の場合はfalseを返します。
 * @param item この[ItemStack]が持っているかどうか判定される要素
 * @return この[ItemStack]が持っている場合はtrue，それ以外はfalse
 */
fun ItemStack.isOf(item: ItemConvertible): Boolean = this.item == item.asItem()

//    ItemUsageContext    //

/**
 * この[ItemUsageContext]が持っている[net.minecraft.world.World]と[net.minecraft.util.math.BlockPos]から[BlockState]を取得します。
 */
val ItemUsageContext.blockState: BlockState
    get() = world.getBlockState(blockPos)

/**
 * この[ItemUsageContext]が持っている[net.minecraft.world.World]と[net.minecraft.util.math.BlockPos]から[T]を取得します。
 * @param T [BlockEntity]を継承したクラス
 * @return [net.minecraft.world.World.getBlockEntity]で取得した[BlockEntity]が[T]を継承していない場合はnull
 */
inline fun <reified T : BlockEntity> ItemUsageContext.getBlockEntity(): T? = world.getBlockEntity(blockPos) as? T
