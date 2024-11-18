package dev.robustum.core.extensions

import dev.robustum.core.recipe.RegistryEntryList
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.tag.Tag

//    ItemStack    //

fun ItemStack.isOf(item: ItemConvertible): Boolean = this.item == item.asItem()

fun ItemStack.isIn(tag: Tag<Item>): Boolean = this.item in tag

fun ItemStack.isIn(entryList: RegistryEntryList<Item>): Boolean = this.item in entryList
