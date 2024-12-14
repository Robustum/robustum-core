package dev.robustum.core.extensions

import dev.robustum.core.registry.RegistryEntryList
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier

//    Tag    //

/**
 * 指定された[Tag]から[Identifier]を取得します。
 *
 * [Tag]が[Tag.Identified]を実装していない場合はnullを返します。
 */
val <T : Any> Tag<T>.idOrNull: Identifier?
    get() = (this as? Tag.Identified<T>)?.id

/**
 * 指定された[Tag]の要素を取得します。
 * @return 取得に失敗した場合は[emptyList]を返します。
 */
fun <T : Any> Tag<T>.getSafeValue(): List<T> = runCatching { values() }.getOrDefault(emptyList())

/**
 * 指定された[Entity]が[tag]に含まれているか判定します。
 * @return [tag]に含まれている場合はtrue
 */
fun Entity.isIn(tag: Tag<EntityType<*>>): Boolean = this.type.isIn(tag)

/**
 * 指定された[ItemStack]が[tag]に含まれているか判定します。
 * @return [tag]に含まれている場合はtrue
 */
fun ItemStack.isIn(tag: Tag<Item>): Boolean = this.item.isIn(tag)

//    RegistryEntryList    //

/**
 * 指定された[BlockState]が[entryList]に含まれているか判定します。
 * @return [entryList]に含まれている場合はtrue
 */
fun BlockState.isIn(entryList: RegistryEntryList<Block>): Boolean = this.block in entryList

/**
 * 指定された[FluidState]が[entryList]に含まれているか判定します。
 * @return [entryList]に含まれている場合はtrue
 */
fun FluidState.isIn(entryList: RegistryEntryList<Fluid>): Boolean = this.fluid in entryList

/**
 * 指定された[Entity]が[entryList]に含まれているか判定します。
 * @return [entryList]に含まれている場合はtrue
 */
fun Entity.isIn(entryList: RegistryEntryList<EntityType<*>>): Boolean = this.type in entryList

/**
 * 指定された[ItemStack]が[entryList]に含まれているか判定します。
 * @return [entryList]に含まれている場合はtrue
 */
fun ItemStack.isIn(entryList: RegistryEntryList<Item>): Boolean = this.item in entryList
