package dev.robustum.core.codec

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapLike
import java.util.*
import kotlin.jvm.optionals.getOrElse

class OptionalCodec<A : Any>(val codec: Codec<A>) : Codec<Optional<A>> {
    override fun <T : Any> encode(input: Optional<A>, ops: DynamicOps<T>, prefix: T): DataResult<T> = when (input.isEmpty) {
        true -> DataResult.success(ops.emptyMap())
        false -> codec.encode(input.get(), ops, prefix)
    }

    private fun <T : Any> isEmpty(ops: DynamicOps<T>, input: T): Boolean = ops
        .getMap(input)
        .result()
        .map { mapLike: MapLike<T> -> mapLike.entries().findAny().isEmpty }
        .getOrElse { false }

    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<Optional<A>, T>> = when {
        isEmpty(ops, input) -> DataResult.success(Pair.of(Optional.empty(), input))
        else -> codec.decode(ops, input).map { pair: Pair<A, T> -> pair.mapFirst(Optional<A>::of) }
    }
}
