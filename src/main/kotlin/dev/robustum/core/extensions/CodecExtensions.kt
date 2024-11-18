package dev.robustum.core.extensions

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapLike
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.registry.Registry
import java.util.*

//    Codec    //

private val ITEM_CODEC: Codec<Item> = lazeCodec { Registry.ITEM }.validate { item: Item ->
    when (item) {
        Items.AIR -> DataResult.error("Item must not be minecraft:air")
        else -> DataResult.success(item)
    }
}

private val RAW_STACK_CODEC: Codec<ItemStack> = RecordCodecBuilder.create { instance ->
    instance
        .group(
            ITEM_CODEC.fieldOf("id").forGetter(ItemStack::getItem),
            Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("count", 1).forGetter(ItemStack::getCount),
            NbtCompound.CODEC.optionalFieldOf("tag").forGetter { stack: ItemStack -> Optional.ofNullable(stack.tag) },
        ).apply(instance) { item: Item, count: Int, nbt: Optional<NbtCompound> ->
            ItemStack(item, count).apply { nbt.ifPresent(this::setTag) }
        }
}

val ITEM_STACK_CODEC: Codec<ItemStack> = optionalCodec(RAW_STACK_CODEC).xmap(
    { it.orElse(ItemStack.EMPTY) },
    { if (it.isEmpty) Optional.empty() else Optional.of(it) },
)

fun <A : Any> Codec<A>.validate(validator: (A) -> DataResult<A>): Codec<A> = flatXmap(validator, validator)

fun <A : Any> lazeCodec(getter: () -> Codec<A>): Codec<A> = object : Codec<A> {
    override fun <T : Any> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> = getter().encode(input, ops, prefix)

    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> = getter().decode(ops, input)
}

fun <A : Any> optionalCodec(codec: Codec<A>): Codec<Optional<A>> = object : Codec<Optional<A>> {
    override fun <T : Any> encode(input: Optional<A>, ops: DynamicOps<T>, prefix: T): DataResult<T> = when (input.isEmpty) {
        true -> DataResult.success(ops.emptyMap())
        false -> codec.encode(input.get(), ops, prefix)
    }

    private fun <T : Any> isEmpty(ops: DynamicOps<T>, input: T): Boolean = ops
        .getMap(input)
        .result()
        .map { mapLike: MapLike<T> -> mapLike.entries().findAny().isEmpty }
        .orElse(false)

    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<Optional<A>, T>> = when {
        isEmpty(ops, input) -> DataResult.success(Pair.of(Optional.empty(), input))
        else -> codec.decode(ops, input).map { pair: Pair<A, T> -> pair.mapFirst(Optional<A>::of) }
    }
}
