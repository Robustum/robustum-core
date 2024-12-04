package dev.robustum.core.extensions

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext

//    ItemStack    //

fun ItemStack.isOf(item: ItemConvertible): Boolean = this.item == item.asItem()

//    ItemUsageContext    //

val ItemUsageContext.blockState: BlockState
    get() = world.getBlockState(blockPos)

inline fun <reified T : BlockEntity> ItemUsageContext.getBlockEntity(): T? = world.getBlockEntity(blockPos) as? T
