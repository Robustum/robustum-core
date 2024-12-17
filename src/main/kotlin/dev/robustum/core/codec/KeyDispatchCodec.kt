package dev.robustum.core.codec

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.MapDecoder
import com.mojang.serialization.MapEncoder
import com.mojang.serialization.MapLike
import com.mojang.serialization.RecordBuilder
import dev.robustum.core.extensions.isErrored
import java.util.function.Function
import java.util.stream.Stream

class KeyDispatchCodec<K : Any, V : Any>(
    val typeKey: String,
    val keyCodec: Codec<K>,
    val type: (V) -> DataResult<K>,
    val decoder: (K) -> DataResult<out MapDecoder<V>>,
    val encoder: (V) -> DataResult<out MapEncoder<V>>,
) : MapCodec<V>() {
    companion object {
        @JvmStatic
        private fun <K : Any, V : Any> getCodec(
            type: (V) -> DataResult<K>,
            codec: (K) -> DataResult<MapCodec<V>>,
            input: V,
        ): DataResult<MapEncoder<V>> = type(input)
            .flatMap { key: K -> codec(key).map(Function.identity()) }
    }

    constructor(
        typeKey: String,
        keyCodec: Codec<K>,
        type: (V) -> DataResult<K>,
        codec: (K) -> DataResult<MapCodec<V>>,
    ) : this(typeKey, keyCodec, type, codec, { input: V -> getCodec(type, codec, input) })

    private val valueKey = "value"

    override fun <T : Any> keys(ops: DynamicOps<T>): Stream<T> = Stream.of(typeKey, valueKey).map(ops::createString)

    override fun <T : Any> decode(ops: DynamicOps<T>, input: MapLike<T>): DataResult<V> {
        val elementName: T =
            input.get(typeKey) ?: return DataResult.error("Input does not contain a key [$typeKey]: $input")
        return keyCodec.decode(ops, elementName).flatMap { type1: Pair<K, T> ->
            decoder(type1.first).flatMap { elementDecoder: MapDecoder<V> ->
                if (ops.compressMaps()) {
                    input
                        .get(ops.createString(valueKey))
                        ?.let { value: T -> elementDecoder.decoder().parse(ops, value).map(Function.identity()) }
                        ?: return@flatMap DataResult.error("Input does not have a \"value\" entry: $input")
                } else {
                    elementDecoder.decode(ops, input).map(Function.identity())
                }
            }
        }
    }

    override fun <T : Any> encode(input: V, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T> {
        val encodeResult: DataResult<out MapEncoder<V>> = encoder(input)
        val builder: RecordBuilder<T> = prefix.withErrorsFrom(encodeResult)
        if (encodeResult.isErrored) {
            Result
            return builder
        }
        val elementEncoder: MapEncoder<V> = encodeResult.result().get()
        return when {
            ops.compressMaps() ->
                prefix
                    .add(typeKey, type(input).flatMap { type1: K -> keyCodec.encodeStart(ops, type1) })
                    .add(valueKey, elementEncoder.encoder().encodeStart(ops, input))

            else ->
                elementEncoder
                    .encode(input, ops, prefix)
                    .add(typeKey, type(input).flatMap { type1: K -> keyCodec.encodeStart(ops, type1) })
        }
    }
}
