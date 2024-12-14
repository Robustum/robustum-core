package dev.robustum.core.extensions

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import net.minecraft.util.collection.DefaultedList
import java.util.*
import java.util.function.Function
import java.util.stream.Stream

//    Codec    //

/**
 * [validator]で[Codec]を検証する
 */
fun <A : Any> Codec<A>.validate(validator: (A) -> DataResult<A>): Codec<A> = flatXmap(validator, validator)

/**
 * [getter]で[Codec]を遅延評価する
 */
fun <A : Any> lazyCodec(getter: () -> Codec<A>): Codec<A> = object : Codec<A> {
    override fun <T : Any> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> = getter().encode(input, ops, prefix)

    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> = getter().decode(ops, input)
}

fun <A : Any, S : Any> Codec<A>.dispatch(type: Function<S, A>, codec: Function<A, MapCodec<S>>): Codec<S> = dispatch("type", type, codec)

fun <A : Any, S : Any> Codec<A>.dispatch(typeKey: String, type: Function<S, A>, codec: Function<A, MapCodec<S>>): Codec<S> =
    dispatchPartial(typeKey, type.andThen(DataResult<A>::success), codec.andThen(DataResult<A>::success))

fun <A : Any, S : Any> Codec<A>.dispatchPartial(
    typeKey: String,
    type: Function<S, DataResult<A>>,
    codec: Function<A, DataResult<MapCodec<S>>>,
): Codec<S> = KeyDispatchCodec(typeKey, this, type::apply, codec::apply).codec()

private class KeyDispatchCodec<K : Any, V : Any>(
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

fun <A : Any> Codec<A>.optionalOf(): Codec<Optional<A>> = object : Codec<Optional<A>> {
    override fun <T : Any> encode(input: Optional<A>, ops: DynamicOps<T>, prefix: T): DataResult<T> = when (input.isEmpty) {
        true -> DataResult.success(ops.emptyMap())
        false -> this@optionalOf.encode(input.get(), ops, prefix)
    }

    private fun <T : Any> isEmpty(ops: DynamicOps<T>, input: T): Boolean = ops
        .getMap(input)
        .result()
        .map { mapLike: MapLike<T> -> mapLike.entries().findAny().isEmpty }
        .orElse(false)

    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<Optional<A>, T>> = when {
        isEmpty(ops, input) -> DataResult.success(Pair.of(Optional.empty(), input))
        else -> this@optionalOf.decode(ops, input).map { pair: Pair<A, T> -> pair.mapFirst(Optional<A>::of) }
    }
}

/**
 * [defaultValue]を初期値にもつ[DefaultedList]の[Codec]に変換する
 */
inline fun <reified A : Any> Codec<List<A>>.defaultedListOf(defaultValue: A): Codec<DefaultedList<A>> =
    xmap({ DefaultedList.copyOf(defaultValue, *it.toTypedArray()) }, Function.identity())

//    DataResult    //

/**
 * [DataResult]が成功しているかどうかを返す
 */
val <R : Any> DataResult<R>.isSucceeded: Boolean
    get() = result().isPresent

/**
 * [DataResult]が失敗しているかどうかを返す
 */
val <R : Any> DataResult<R>.isErrored: Boolean
    get() = error().isPresent

/**
 * [DataResult]が成功しているときのみ[action]を実行する
 */
fun <R : Any> DataResult<R>.ifSucceeded(action: (R) -> Unit): DataResult<R> = apply { result().ifPresent(action) }

/**
 * [DataResult]が失敗しているときのみ[action]を実行する
 */
fun <R : Any> DataResult<R>.ifErrored(action: (DataResult.PartialResult<R>) -> Unit): DataResult<R> = apply { error().ifPresent(action) }
