@file:OptIn(ExperimentalContracts::class)

package dev.robustum.core.codec

import com.mojang.datafixers.kinds.App
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.MapLike
import com.mojang.serialization.RecordBuilder
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.robustum.core.extensions.convert
import dev.robustum.core.extensions.isSucceeded
import dev.robustum.core.extensions.listOrElement
import dev.robustum.core.extensions.toCodec
import dev.robustum.core.extensions.validate
import dev.robustum.core.mixin.codec.IngredientAccessor
import dev.robustum.core.recipe.ItemIngredient
import dev.robustum.core.registry.RegistryEntryList
import dev.robustum.core.util.DFUPair
import dev.robustum.core.util.Either
import dev.robustum.core.util.Ior
import dev.robustum.core.util.Option
import dev.robustum.core.util.getOrElse
import dev.robustum.core.util.kotlin
import dev.robustum.core.util.some
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.Ingredient
import net.minecraft.tag.ServerTagManagerHolder
import net.minecraft.tag.Tag
import net.minecraft.tag.TagGroup
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.registry.DefaultedRegistry
import net.minecraft.util.registry.Registry
import java.util.stream.Stream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.enums.enumEntries

data object RobustumCodecs {
    @JvmField
    val ANY: Codec<Any> = KotlinOps.toCodec()

    @JvmField
    val DYE_COLOR: Codec<DyeColor> = stringEnum(DyeColor::asString)

    /**
     * [Map]の[Codec]を作成します。
     * @param K キーとなるクラス
     * @param V 値となるクラス
     * @param keyCodec キーの[Codec]
     * @param valueCodec 値の[Codec]
     */
    @JvmStatic
    fun <K : Any, V : Any> mapOf(keyCodec: Codec<K>, valueCodec: Codec<V>): Codec<Map<K, V>> = Codec.unboundedMap(keyCodec, valueCodec)

    /**
     * [Option]でラップされた[Codec]を作成します。
     */
    @JvmStatic
    fun <A : Any> option(codec: Codec<A>): Codec<Option<A>> = OptionCodec(codec)

    @JvmInline
    private value class OptionCodec<A : Any>(private val codec: Codec<A>) : Codec<Option<A>> {
        override fun <T> encode(input: Option<A>, ops: DynamicOps<T>, prefix: T): DataResult<T> = input.fold(
            { DataResult.success(ops.emptyMap()) },
            { codec.encode(it, ops, prefix) },
        )

        private fun <T> isEmptyMap(ops: DynamicOps<T>, input: T): Boolean = ops.getMap(input).result().kotlin.fold(
            { false },
            { it.entries().findAny().isEmpty },
        )

        override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<DFUPair<Option<A>, T>> = when {
            isEmptyMap(ops, input) -> DataResult.success(DFUPair.of(Option.none(), input))
            else -> codec.decode(ops, input).map { pair: DFUPair<A, T> -> pair.mapFirst { it.some() } }
        }
    }

    /**
     * [Either]の[Codec]を作成します。
     * @param A 左側の値となるクラス
     * @param B 右側の値となるクラス
     * @param left 左側の値の[Codec]
     * @param right 右側の値の[Codec]
     * @see Codec.either
     */
    @JvmStatic
    fun <A, B> either(left: Codec<A>, right: Codec<B>): Codec<Either<A, B>> = HTEitherCodec(left, right, false)

    /**
     * [Either]の[Codec]を作成します。
     * @param A 左側の値となるクラス
     * @param B 右側の値となるクラス
     * @param left 左側の値の[Codec]
     * @param right 右側の値の[Codec]
     * @see Codec.xor
     */
    @JvmStatic
    fun <A, B> xor(left: Codec<A>, right: Codec<B>): Codec<Either<A, B>> = HTEitherCodec(left, right, true)

    /**
     * @see com.mojang.serialization.codecs.EitherCodec
     * @see com.mojang.serialization.codecs.XorCodec
     */
    private class HTEitherCodec<A, B>(val left: Codec<A>, val right: Codec<B>, val isStrict: Boolean) : Codec<Either<A, B>> {
        override fun <T : Any> encode(input: Either<A, B>, ops: DynamicOps<T>, prefix: T): DataResult<T> = input.fold(
            { left.encode(it, ops, prefix) },
            { right.encode(it, ops, prefix) },
        )

        override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<DFUPair<Either<A, B>, T>> {
            val leftRead: DataResult<DFUPair<Either<A, B>, T>> = left.decode(ops, input).map { it.mapFirst { Either.Left(it) } }
            val rightRead: DataResult<DFUPair<Either<A, B>, T>> = right.decode(ops, input).map { it.mapFirst { Either.Right(it) } }
            val leftResult: Option<DFUPair<Either<A, B>, T>> = leftRead.result().kotlin
            val rightResult: Option<DFUPair<Either<A, B>, T>> = rightRead.result().kotlin
            if (isStrict && (leftResult.isSome() && rightResult.isSome())) {
                return DataResult.error(
                    "Both alternatives read successfully, can not pick the correct one; first: ${leftResult.getOrNull()} second: ${rightResult.getOrNull()}",
                    leftResult.getOrNull(),
                )
            }
            if (leftResult.isSome()) {
                return leftRead
            }
            if (rightResult.isSome()) {
                return rightRead
            }
            return leftRead.apply2({ _, second -> second }, rightRead)
        }
    }

    /**
     * [Ior]の[MapCodec]を作成します。
     * @param A 左側の値となるクラス
     * @param B 右側の値となるクラス
     * @param left 左側の値の[MapCodec]
     * @param right 右側の値の[MapCodec]
     */
    @JvmStatic
    fun <A, B> ior(left: MapCodec<A>, right: MapCodec<B>): MapCodec<Ior<A, B>> = HTIorMapCodec(left, right)

    private class HTIorMapCodec<A, B>(val left: MapCodec<A>, val right: MapCodec<B>) : MapCodec<Ior<A, B>>() {
        override fun <T : Any> keys(ops: DynamicOps<T>): Stream<T> = Stream.concat(left.keys(ops), right.keys(ops))

        override fun <T : Any> decode(ops: DynamicOps<T>, input: MapLike<T>): DataResult<Ior<A, B>> {
            val leftResult: DataResult<A> = left.decode(ops, input)
            val rightResult: DataResult<B> = right.decode(ops, input)

            val bothResult: DataResult<Ior<A, B>> = leftResult.flatMap { leftIn: A ->
                rightResult.map { rightIn: B -> Ior.Both(leftIn, rightIn) }
            }
            if (bothResult.isSucceeded) return bothResult
            if (leftResult.isSucceeded) {
                return when {
                    rightResult.isSucceeded ->
                        leftResult.flatMap { leftIn: A ->
                            rightResult.map { rightIn: B -> Ior.Both(leftIn, rightIn) }
                        }
                    else -> leftResult.map { Ior.Left(it) }
                }
            } else {
                return when {
                    rightResult.isSucceeded -> rightResult.map { Ior.Right(it) }
                    else -> run {
                        val leftError: String = leftResult.error().orElseThrow().message()
                        val rightError: String = rightResult.error().orElseThrow().message()
                        DataResult.error("Failed to parse ior. Left: $leftError; Right: $rightError;")
                    }
                }
            }
        }

        override fun <T : Any> encode(input: Ior<A, B>, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T> = input.fold(
            { left.encode(it, ops, prefix) },
            { right.encode(it, ops, prefix) },
            { left: A, right: B ->
                this.left.encode(left, ops, prefix)
                this.right.encode(right, ops, prefix)
            },
        )
    }

    /**
     * [Enum]の[Codec]を返します。
     * @param V [Enum]を継承したクラス
     * @param factory [V]を[String]に変換するブロック
     */
    @JvmStatic
    inline fun <reified V : Enum<V>> stringEnum(crossinline factory: (V) -> String?): Codec<V> = Codec.STRING.flatXmap<V>(
        { name: String ->
            enumEntries<V>().firstOrNull { factory(it) == name }?.let { DataResult.success(it) }
                ?: DataResult.error("Unknown element name: $name")
        },
        { value: V -> factory(value)?.let { DataResult.success(it) } ?: DataResult.error("Element with unknown name: $value") },
    )

    /**
     * [RecordCodecBuilder.mapCodec]を最適化した代替
     */
    @JvmStatic
    inline fun <O> recordMap(builder: (RecordCodecBuilder.Instance<O>) -> App<RecordCodecBuilder.Mu<O>, O>): MapCodec<O> {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }
        return RecordCodecBuilder.build(builder(RecordCodecBuilder.instance()))
    }

    /**
     * [RecordCodecBuilder.create]を最適化した代替
     */
    @JvmStatic
    inline fun <O> record(builder: (RecordCodecBuilder.Instance<O>) -> App<RecordCodecBuilder.Mu<O>, O>): Codec<O> {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }
        return recordMap(builder).codec()
    }

    fun <A> lazy(delegate: () -> Codec<A>): Codec<A> = object : Codec<A> {
        override fun <T : Any> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> = delegate().encode(input, ops, prefix)

        override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<DFUPair<A, T>> = delegate().decode(ops, input)
    }

    //    Ranged    //

    /**
     * [ClosedRange]で値の範囲を制限した[Codec]を作成します。
     * @param T [Comparable]を実装したクラス
     * @param codec 元となる[Codec]
     * @param range 値の範囲
     */
    @JvmStatic
    fun <T : Comparable<T>> ranged(codec: Codec<T>, range: ClosedRange<T>): Codec<T> = codec.validate { number: T ->
        when (number) {
            in range -> DataResult.success(number)
            else -> DataResult.error("Value must be within range $range: $number")
        }
    }

    /**
     * `0`以上の値を対象とする[Int]の[Codec]
     */
    @JvmField
    val NON_NEGATIVE_INT: Codec<Int> = ranged(Codec.INT, 0..Int.MAX_VALUE)

    /**
     * `0`以上の値を対象とする[Long]の[Codec]
     */
    @JvmField
    val NON_NEGATIVE_LONG: Codec<Long> = ranged(Codec.LONG, 0..Long.MAX_VALUE)

    /**
     * `1`以上の値を対象とする[Int]の[Codec]
     */
    @JvmField
    val POSITIVE_INT: Codec<Int> = ranged(Codec.INT, 1..Int.MAX_VALUE)

    /**
     * `1`以上の値を対象とする[Long]の[Codec]
     */
    @JvmField
    val POSITIVE_LONG: Codec<Long> = ranged(Codec.LONG, 1..Long.MAX_VALUE)

    //    Registry    //

    fun <T : Any> nonEmptyRegistry(registry: DefaultedRegistry<T>, message: () -> String): Codec<T> = registry.validate { element: T ->
        if (registry.getId(element) == registry.defaultId) {
            DataResult.error(message())
        } else {
            DataResult.success(element)
        }
    }

    /**
     * [Blocks.AIR]を受け付けない[Block]の[Codec]です。
     */
    @JvmField
    val BLOCK: Codec<Block> = nonEmptyRegistry(Registry.BLOCK) { "Block must be non-empty" }

    /**
     * [Fluids.EMPTY]を受け付けない[Fluid]の[Codec]です。
     */
    @JvmField
    val FLUID: Codec<Fluid> = nonEmptyRegistry(Registry.FLUID) { "Fluid must be non-empty" }

    /**
     * [Items.AIR]を受け付けない[Item]の[Codec]です。
     */
    @JvmField
    val ITEM: Codec<Item> = nonEmptyRegistry(Registry.ITEM) { "Item must be non-empty" }

    @JvmStatic
    private val RAW_STACK: Codec<ItemStack> = record { instance ->
        instance
            .group(
                ITEM.fieldOf("id").forGetter(ItemStack::getItem),
                Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("count", 1).forGetter(ItemStack::getCount),
                NbtCompound.CODEC
                    .optionalFieldOf("tag")
                    .convert()
                    .forGetter { stack: ItemStack -> Option.fromNullable(stack.tag) },
            ).apply(instance) { item: Item, count: Int, nbt: Option<NbtCompound> ->
                val stack = ItemStack(item, count)
                nbt.onSome(stack::setTag)
                stack
            }
    }

    /**
     * [ItemStack.isEmpty]を返す[ItemStack]も受け付ける[ItemStack]の[Codec]です。
     */
    @JvmField
    val ITEM_STACK: Codec<ItemStack> = option(RAW_STACK).xmap(
        { it.getOrElse { ItemStack.EMPTY } },
        { it.some().filterNot(ItemStack::isEmpty) },
    )

    //    Tag    //

    @JvmStatic
    fun <T : Any> hashedTag(group: () -> TagGroup<T>): Codec<Tag<T>> = Codec.STRING.flatXmap(
        { value: String ->
            if (value.startsWith("#")) {
                Identifier.method_29186(value.removePrefix("#")).flatMap { id: Identifier ->
                    group().getTag(id)?.let(DataResult<Tag<T>>::success) ?: DataResult.error("Unknown tag: $id")
                }
            } else {
                DataResult.error("Not a tag id")
            }
        },
        { tag: Tag<T> ->
            val id: Identifier = group().getUncheckedTagId(tag) ?: return@flatXmap DataResult.error("Unknown tag: $tag")
            DataResult.success("#$id")
        },
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
    val INGREDIENT: Codec<Ingredient> = EntryOrTag.ITEM.xmap(
        { ItemIngredient(it).vanillaIngredient },
    ) { ingredient: Ingredient ->
        val empty: RegistryEntryList<Item> = RegistryEntryList.empty()
        if (ingredient.isEmpty) {
            empty
        } else {
            val entries: Array<out Ingredient.Entry> = (ingredient as IngredientAccessor).entries
            if (entries.size == 1) {
                when (val entry: Ingredient.Entry = entries[0]) {
                    is Ingredient.StackEntry -> RegistryEntryList.direct(entry.stacks.first().item)
                    is Ingredient.TagEntry -> RegistryEntryList.tagged(entry.tag)
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
        if (ingredient.isEmpty) {
            DataResult.error("Empty ingredient is not allowed!")
        } else {
            DataResult.success(ingredient)
        }
    }

    //    EntryOrTag    //

    data object EntryOrTag {
        @JvmField
        val BLOCK: Codec<RegistryEntryList<Block>> = create(Registry.BLOCK) { ServerTagManagerHolder.getTagManager().blocks }

        @JvmField
        val ENTITY_TYPE: Codec<RegistryEntryList<EntityType<*>>> = create(Registry.ENTITY_TYPE) {
            ServerTagManagerHolder.getTagManager().entityTypes
        }

        @JvmField
        val FLUID: Codec<RegistryEntryList<Fluid>> = create(Registry.FLUID) { ServerTagManagerHolder.getTagManager().fluids }

        @JvmField
        val ITEM: Codec<RegistryEntryList<Item>> = create(Registry.ITEM) { ServerTagManagerHolder.getTagManager().items }

        @JvmStatic
        private fun <T : Any> create(registry: Registry<T>, group: () -> TagGroup<T>): Codec<RegistryEntryList<T>> =
            either(registry.listOrElement(), hashedTag(group))
                .xmap(
                    { either: Either<List<T>, Tag<T>> ->
                        either.fold(RegistryEntryList.Companion::direct, RegistryEntryList.Companion::tagged)
                    },
                    RegistryEntryList<T>::unwrap,
                )
    }
}
