package dev.robustum.core.extensions

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tag.Tag
import net.minecraft.tag.TagGroup
import net.minecraft.util.Identifier

//    Tag    //

/**
 * 指定された[Tag]から[Identifier]を取得します。
 *
 * [Tag]が[Tag.Identified]を実装していない場合はnullを返します。
 */
val <T : Any> Tag<T>.idOrNull: Identifier? get() = (this as? Tag.Identified<T>)?.id

/**
 * 指定された[Tag]と[tagGroup]から[Identifier]を取得します。
 * @return [idOrNull]がnullの場合，[TagGroup.getUncheckedTagId]より取得します。
 */
fun <T : Any> Tag<T>.getIdOrNull(tagGroup: TagGroup<T>): Identifier? = idOrNull ?: tagGroup.getUncheckedTagId(this)

/**
 * 指定された[Tag]の要素を取得します。
 * @return 取得に失敗した場合は[emptyList]を返します。
 */
fun <T : Any> Tag<T>.getSafeValue(): List<T> = runCatching { values() }.getOrDefault(emptyList())

operator fun Tag<Block>.contains(state: BlockState): Boolean = this.contains(state.block)

operator fun Tag<EntityType<*>>.contains(entity: Entity): Boolean = this.contains(entity.type)

operator fun Tag<Fluid>.contains(state: FluidState): Boolean = this.contains(state.fluid)

operator fun Tag<Item>.contains(stack: ItemStack): Boolean = this.contains(stack.item)
