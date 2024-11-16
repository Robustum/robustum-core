package dev.robustum.core.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.tag.ServerTagManagerHolder
import net.minecraft.tag.Tag
import net.minecraft.util.registry.Registry
import java.util.function.Predicate

class WeightedIngredient private constructor(val entries: RegistryEntryList<Item>, val count: Int) : Predicate<ItemStack> {
    companion object {
        @JvmField
        val CODEC: Codec<WeightedIngredient> = RecordCodecBuilder.create { instance ->
            instance
                .group(
                    RegistryEntryList
                        .codec(Registry.ITEM, ServerTagManagerHolder.getTagManager()::getItems)
                        .fieldOf("items")
                        .forGetter(WeightedIngredient::entries),
                    Codec.intRange(1, Int.MAX_VALUE).optionalFieldOf("count", 1).forGetter(WeightedIngredient::count),
                ).apply(instance, ::WeightedIngredient)
        }
    }

    constructor(tag: Tag<Item>, count: Int = 1) : this(RegistryEntryList.tag(tag), count)

    constructor(item: Item, count: Int = 1) : this(RegistryEntryList.of(item), count)

    val isEmpty: Boolean
        get() = entries.storage.map({ it.values().isEmpty() }, { it == Items.AIR }) || count <= 0

    val vanillaIngredient: Ingredient
        get() = entries.storage.map(Ingredient::fromTag, Ingredient::ofItems)

    override fun test(stack: ItemStack): Boolean = when (stack.isEmpty) {
        true -> this.isEmpty
        false -> entries.storage.map(
            { stack.item in it },
            { stack.item == it },
        ) &&
            stack.count >= count
    }
}
