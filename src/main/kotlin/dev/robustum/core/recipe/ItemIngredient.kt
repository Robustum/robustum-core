package dev.robustum.core.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.robustum.core.extensions.getEntryOrThrow
import dev.robustum.core.extensions.isIn
import dev.robustum.core.registry.RegistryEntryList
import dev.robustum.core.registry.RegistryEntryListCodec
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.tag.Tag
import net.minecraft.util.registry.Registry
import java.util.function.Predicate

class ItemIngredient private constructor(val entryList: RegistryEntryList<Item>, val count: Int) : Predicate<ItemStack> {
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

        @JvmField
        val VANILLA_CODEC: Codec<Ingredient> = CODEC.xmap(
            { it.vanillaIngredient },
            { ItemIngredient(it.matchingItemIds.map(Registry.ITEM::get)) },
        )
    }

    constructor(tag: Tag<Item>, count: Int = 1) : this(RegistryEntryList.tag(tag), count)

    constructor(item: Item, count: Int = 1) : this(
        RegistryEntryList.of(item, Registry.ITEM::getEntryOrThrow),
        count,
    )

    private constructor(items: List<Item>, count: Int = 1) : this(Tag.of(items.toSet()), count)

    val isEmpty: Boolean
        get() = entryList.isEmpty || count <= 0

    val vanillaIngredient: Ingredient
        get() = entryList.storage.map(Ingredient::fromTag, Ingredient::ofItems)

    override fun test(stack: ItemStack): Boolean = when (stack.isEmpty) {
        true -> this.isEmpty
        false -> stack.isIn(entryList) && stack.count >= count
    }
}
