package dev.robustum.core.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.robustum.core.codec.RegistryEntryListCodec
import dev.robustum.core.extensions.isIn
import dev.robustum.core.registry.RegistryEntryList
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.tag.Tag
import java.util.function.Predicate

class ItemIngredient(val entryList: RegistryEntryList<Item>, val count: Int = 1) : Predicate<ItemStack> {
    companion object {
        @JvmField
        val EMPTY = ItemIngredient(RegistryEntryList.empty(), 0)

        @JvmField
        val CODEC: Codec<ItemIngredient> = RecordCodecBuilder.create { instance ->
            instance
                .group(
                    RegistryEntryListCodec.ITEM
                        .fieldOf("items")
                        .forGetter(ItemIngredient::entryList),
                    Codec.intRange(1, Int.MAX_VALUE).optionalFieldOf("count", 1).forGetter(ItemIngredient::count),
                ).apply(instance, ::ItemIngredient)
        }
    }

    constructor(tag: Tag<Item>, count: Int = 1) : this(RegistryEntryList.ofTag(tag), count)

    constructor(item: Item, count: Int = 1) : this(RegistryEntryList.direct(item), count)

    val isEmpty: Boolean
        get() = entryList.isEmpty || count <= 0

    val vanillaIngredient: Ingredient
        get() = entryList.storage.map(Ingredient::fromTag) { items: List<Item> ->
            items.map(::ItemStack).stream().let(Ingredient::ofStacks)
        }

    override fun test(stack: ItemStack): Boolean = when (stack.isEmpty) {
        true -> this.isEmpty
        false -> stack.isIn(entryList) && stack.count >= count
    }
}
