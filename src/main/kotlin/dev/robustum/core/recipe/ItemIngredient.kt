package dev.robustum.core.recipe

import com.mojang.serialization.Codec
import dev.robustum.core.codec.RobustumCodecs
import dev.robustum.core.registry.RegistryEntryList
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.tag.Tag
import java.util.function.Predicate

/**
 * 個数を受け取る[Ingredient]のクラスです。
 * @param entryList 条件に一致するアイテムのリスト
 * @param count 必要な個数
 */
class ItemIngredient(val entryList: RegistryEntryList<Item>, val count: Int = 1) : Predicate<ItemStack> {
    companion object {
        @JvmField
        val EMPTY = ItemIngredient(RegistryEntryList.empty(), 0)

        @JvmField
        val CODEC: Codec<ItemIngredient> = RobustumCodecs.record { instance ->
            instance
                .group(
                    RobustumCodecs.EntryOrTag.ITEM
                        .fieldOf("items")
                        .forGetter(ItemIngredient::entryList),
                    Codec.intRange(1, Int.MAX_VALUE).optionalFieldOf("count", 1).forGetter(ItemIngredient::count),
                ).apply(instance, ::ItemIngredient)
        }
    }

    constructor(tag: Tag<Item>, count: Int = 1) : this(RegistryEntryList.tagged(tag), count)

    constructor(item: Item, count: Int = 1) : this(RegistryEntryList.direct(item), count)

    /**
     * この素材が有効かどうか判定します。
     */
    val isEmpty: Boolean
        get() = entryList.isEmpty() || count <= 0

    /**
     * この素材をバニラの[Ingredient]に変換します。
     */
    val vanillaIngredient: Ingredient
        get() = when (isEmpty) {
            true -> Ingredient.EMPTY
            false -> entryList.unwrap().fold(
                { items: List<Item> -> items.map(::ItemStack).stream().let(Ingredient::ofStacks) },
                Ingredient::fromTag,
            )
        }

    override fun test(stack: ItemStack): Boolean = when (stack.isEmpty) {
        true -> this.isEmpty
        false -> stack.item in entryList && stack.count >= count
    }
}
