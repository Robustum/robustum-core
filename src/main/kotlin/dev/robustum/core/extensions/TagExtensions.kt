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

val <T : Any> Tag<T>.idOrNull: Identifier?
    get() = (this as? Tag.Identified<T>)?.id

fun <T : Any> Tag<T>.getSafeValue(): List<T> = runCatching { values() }.getOrDefault(listOf())

fun Entity.isIn(tag: Tag<EntityType<*>>): Boolean = this.type.isIn(tag)

fun ItemStack.isIn(tag: Tag<Item>): Boolean = this.item.isIn(tag)

//    RegistryEntryList    //

fun BlockState.isIn(entryList: RegistryEntryList<Block>): Boolean = this.block in entryList

fun FluidState.isIn(entryList: RegistryEntryList<Fluid>): Boolean = this.fluid in entryList

fun Entity.isIn(entryList: RegistryEntryList<EntityType<*>>): Boolean = this.type in entryList

fun ItemStack.isIn(entryList: RegistryEntryList<Item>): Boolean = this.item in entryList
