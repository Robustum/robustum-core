package dev.robustum.core.codec

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.robustum.core.extensions.lazyCodec
import dev.robustum.core.extensions.optionalOf
import dev.robustum.core.extensions.validate
import dev.robustum.core.mixin.codec.IngredientAccessor
import dev.robustum.core.recipe.ItemIngredient
import dev.robustum.core.registry.RegistryEntryList
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.Ingredient
import net.minecraft.util.registry.Registry
import java.util.*

object RobustumCodecs {
    //    Block    //
    /**
     * [net.minecraft.block.Blocks.AIR]を受け付けない[net.minecraft.block.Block]の[com.mojang.serialization.Codec]です。
     */
    @JvmField
    val BLOCK: Codec<Block> = lazyCodec { Registry.BLOCK }.validate { block: Block ->
        when (block) {
            Blocks.AIR -> DataResult.error("Block must not be minecraft:air")
            else -> DataResult.success(block)
        }
    }

    //    ItemStack    //
    /**
     * [net.minecraft.item.Items.AIR]を受け付けない[net.minecraft.item.Item]の[Codec]です。
     */
    @JvmField
    val ITEM: Codec<Item> = lazyCodec { Registry.ITEM }.validate { item: Item ->
        when (item) {
            Items.AIR -> DataResult.error("Item must not be minecraft:air")
            else -> DataResult.success(item)
        }
    }

    @JvmStatic
    private val RAW_STACK: Codec<ItemStack> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                ITEM.fieldOf("id").forGetter(ItemStack::getItem),
                Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("count", 1).forGetter(ItemStack::getCount),
                NbtCompound.CODEC
                    .optionalFieldOf("tag")
                    .forGetter { stack: ItemStack -> Optional.ofNullable(stack.tag) },
            ).apply(instance) { item: Item, count: Int, nbt: Optional<NbtCompound> ->
                ItemStack(item, count).apply { nbt.ifPresent(this::setTag) }
            }
    }

    /**
     * [ItemStack.isEmpty]を返す[ItemStack]も受け付ける[ItemStack]の[Codec]です。
     */
    @JvmField
    val ITEM_STACK: Codec<ItemStack> = RAW_STACK.optionalOf().xmap(
        { it.orElse(ItemStack.EMPTY) },
        { if (it.isEmpty) Optional.empty() else Optional.of(it) },
    )

    //    Ingredient    //
    /**
     * よりシンプルな記法で書ける[Ingredient]の[Codec]です。
     * ```
     * "minecraft:dirt" -> Ingredient.ofItems(Items.DIRT)
     * ["minecraft:dirt", "minecraft:stone"] -> Ingredient.ofItems(Items.DIRT, Items.STONE)
     * "#minecraft:wool" -> Ingredient.fromTag(ItemTags.WOOL)
     * ```
     */
    @Suppress("KotlinConstantConditions")
    @JvmField
    val INGREDIENT: Codec<Ingredient> = RegistryEntryListCodec.ITEM.xmap(
        { ItemIngredient(it).vanillaIngredient },
    ) { ingredient: Ingredient ->
        val empty: RegistryEntryList<Item> = RegistryEntryList.empty()
        if (ingredient.isEmpty) {
            empty
        } else {
            val entries: Array<out Ingredient.Entry> = (ingredient as IngredientAccessor).entries
            entries
                .runCatching {
                    val items: List<Item> = entries
                        .flatMap(Ingredient.Entry::getStacks)
                        .map(ItemStack::getItem)
                        .distinct()
                    when (items.size) {
                        0 -> empty

                        1 -> RegistryEntryList.direct(items[0])

                        else -> RegistryEntryList.direct(items)
                    }
                }.getOrDefault(empty)
        }
    }
}
