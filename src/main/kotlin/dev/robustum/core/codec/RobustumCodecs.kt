package dev.robustum.core.codec

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.robustum.core.extensions.*
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
import net.minecraft.tag.Tag
import net.minecraft.tag.TagGroup
import net.minecraft.text.*
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.*
import java.util.function.Function

object RobustumCodecs {
    //    Any    //

    @JvmField
    val ANY: Codec<Any> = KotlinOps.toCodec()

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
    @Suppress("CAST_NEVER_SUCCEEDS")
    @JvmField
    val INGREDIENT: Codec<Ingredient> = RegistryEntryListCodec.ITEM.xmap(
        { ItemIngredient(it).vanillaIngredient },
    ) { ingredient: Ingredient ->
        val empty: RegistryEntryList<Item> = RegistryEntryList.empty()
        if (ingredient.isEmpty) {
            empty
        } else {
            val entries: Array<out Ingredient.Entry> = (ingredient as IngredientAccessor).entries
            if (entries.size == 1) {
                val entry: Ingredient.Entry = entries[0]
                when (entry) {
                    is Ingredient.StackEntry -> RegistryEntryList.direct(entry.stacks.first().item)
                    is Ingredient.TagEntry -> RegistryEntryList.ofTag(entry.tag)
                    else -> empty
                }
            } else {
                val stackEntries: List<Ingredient.StackEntry> = entries.filterIsInstance<Ingredient.StackEntry>()
                if (stackEntries.isEmpty()) {
                    empty
                } else {
                    val items: List<Item> = stackEntries
                        .flatMap(Ingredient.StackEntry::getStacks)
                        .map(ItemStack::getItem)
                        .distinct()
                    when (items.size) {
                        0 -> empty

                        1 -> RegistryEntryList.direct(items[0])

                        else -> RegistryEntryList.direct(items)
                    }
                }
            }
        }
    }

    @JvmField
    val NON_EMPTY_INGREDIENT: Codec<Ingredient> = INGREDIENT.validate { ingredient: Ingredient ->
        DataResult.success(ingredient).filterNot(Ingredient::isEmpty, "Empty ingredient is not allowed!")
    }

    //    Tag    //

    @JvmStatic
    fun <T : Any> identifiedTagCodec(groupGetter: () -> TagGroup<T>): Codec<Tag<T>> = Identifier.CODEC.flatXmap(
        { groupGetter().getTag(it).toDataResult("Unknown tag: $it") },
        { it.getIdOrNull(groupGetter()).toDataResult("Unknown tag: $it") },
    )

    //    Text    //

    @JvmField
    val TEXT: Codec<Text> = lazyCodec {
        anyCodec(
            RAW_LITERAL,
            LITERAL,
            TRANSLATE,
            SCORE,
            SELECTOR,
            KEY_BIND,
            NBT_TEXT,
        )
    }

    @JvmField
    val RAW_LITERAL: Codec<LiteralText> = Codec.STRING.xmap(::LiteralText, LiteralText::asString)

    @JvmField
    val LITERAL: Codec<LiteralText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("text").forGetter(LiteralText::asString),
            ).apply(instance, ::LiteralText)
    }

    @JvmStatic
    private val ARGS: Codec<in Any> = Codec
        .either(ANY, TEXT)
        .xmap(
            { either: Either<in Any, Text> -> either.map(Function.identity(), Text::asString) },
            { arg: Any -> if (arg is Text) Either.right(arg) else Either.left(arg) },
        )

    @JvmField
    val TRANSLATE: Codec<TranslatableText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("translate").forGetter(TranslatableText::getKey),
                ARGS.listOf().optionalFieldOf("with", listOf()).forGetter { it.args.toList() },
            ).apply(instance) { key: String, args: List<Any> -> TranslatableText(key, *args.toTypedArray()) }
    }

    @JvmStatic
    private val RAW_SCORE: Codec<Pair<String, String>> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("name").forGetter(Pair<String, String>::first),
                Codec.STRING.fieldOf("objective").forGetter(Pair<String, String>::second),
            ).apply(instance, ::Pair)
    }

    @JvmField
    val SCORE: Codec<ScoreText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                RAW_SCORE.fieldOf("score").forGetter { it.name to it.objective },
            ).apply(instance) { raw: Pair<String, String> -> ScoreText(raw.first, raw.second) }
    }

    @JvmField
    val SELECTOR: Codec<SelectorText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("selector").forGetter(SelectorText::asString),
            ).apply(instance, ::SelectorText)
    }

    @JvmField
    val KEY_BIND: Codec<KeybindText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("keybind").forGetter(KeybindText::getKey),
            ).apply(instance, ::KeybindText)
    }

    @JvmStatic
    private val BLOCK_NBT_TEXT: Codec<NbtText.BlockNbtText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("nbt").forGetter(NbtText.BlockNbtText::getPath),
                Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.BlockNbtText::shouldInterpret),
                Codec.STRING.fieldOf("block").forGetter(NbtText.BlockNbtText::getPos),
            ).apply(instance, NbtText::BlockNbtText)
    }

    @JvmStatic
    private val ENTITY_NBT_TEXT: Codec<NbtText.EntityNbtText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("nbt").forGetter(NbtText.EntityNbtText::getPath),
                Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.EntityNbtText::shouldInterpret),
                Codec.STRING.fieldOf("entity").forGetter(NbtText.EntityNbtText::getSelector),
            ).apply(instance, NbtText::EntityNbtText)
    }

    @JvmStatic
    private val STORAGE_NBT_TEXT: Codec<NbtText.StorageNbtText> = RecordCodecBuilder.create { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("nbt").forGetter(NbtText.StorageNbtText::getPath),
                Codec.BOOL.optionalFieldOf("interpret", false).forGetter(NbtText.StorageNbtText::shouldInterpret),
                Identifier.CODEC.fieldOf("storage").forGetter(NbtText.StorageNbtText::getId),
            ).apply(instance, NbtText::StorageNbtText)
    }

    @JvmField
    val NBT_TEXT: Codec<NbtText> = anyCodec(
        BLOCK_NBT_TEXT,
        ENTITY_NBT_TEXT,
        STORAGE_NBT_TEXT,
    )
}
