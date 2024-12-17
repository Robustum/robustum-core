package dev.robustum.core.extensions

import com.mojang.datafixers.util.Either
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import dev.robustum.core.codec.KeyDispatchCodec
import dev.robustum.core.codec.OptionalCodec
import net.minecraft.util.collection.DefaultedList
import java.util.*
import java.util.function.Function
import kotlin.jvm.optionals.getOrNull

typealias DataPair<F, S> = Pair<F, S>

//    Encoder    //

/**
 * 指定された[input]を[B]にキャストしてエンコードします。
 * @param A [input]のクラス
 * @param B [A]を継承したクラス
 * @param T [ops]のクラス
 * @return [A]を[B]にキャストできなかった場合は[DataResult.error]
 */
@Suppress("UNCHECKED_CAST")
fun <A : Any, B : A, T : Any> Encoder<B>.forceEncode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> =
    (input as? B)?.let { encode(it, ops, prefix) } ?: DataResult.error("Failed to encode!")

//    Decoder    //

//    Codec    //

/**
 * 指定された[getter]で遅延評価された[Codec]を返します。
 * @param A 値のクラス
 * @param getter 遅延評価された元の[Codec]
 * @return [getter]で遅延評価された[Codec]
 */
fun <A : Any> lazyCodec(getter: () -> Codec<A>): Codec<A> = object : Codec<A> {
    override fun <T : Any> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> = getter().encode(input, ops, prefix)

    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> = getter().decode(ops, input)
}

fun <A : Any> anyCodec(vararg child: Codec<out A>): Codec<A> = anyCodec(child.toList())

/**
 * 指定された[children]のいずれかで変換する[Codec]を返します。
 */
fun <A : Any> anyCodec(children: List<Codec<out A>>): Codec<A> = object : Codec<A> {
    override fun <T : Any> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        for (codec: Codec<out A> in children) {
            runCatching {
                val result: DataResult<T> = codec.forceEncode(input, ops, prefix)
                if (result.isSucceeded) {
                    return result
                }
            }
        }
        return DataResult.error("Failed to encode input!")
    }

    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> {
        for (codec: Codec<out A> in children) {
            val result: DataResult<out Pair<out A, T>> = codec.decode(ops, input)
            if (result.isSucceeded) {
                return result.map { pair: Pair<out A, T> -> pair.mapFirst { it as A } }
            }
        }
        return DataResult.error("Failed to decode input!")
    }
}

/**
 * 指定された[validator]で検証した[Codec]を返します。
 * @param A 値のクラス
 * @param validator 値を[DataResult]で評価する。
 * @return [validator]で評価された[Codec]
 */
fun <A : Any> Codec<A>.validate(validator: (A) -> DataResult<A>): Codec<A> = flatXmap(validator, validator)

fun <A : Any, S : Any> Codec<A>.dispatch(type: Function<S, A>, codec: Function<A, MapCodec<S>>): Codec<S> = dispatch("type", type, codec)

fun <A : Any, S : Any> Codec<A>.dispatch(typeKey: String, type: Function<S, A>, codec: Function<A, MapCodec<S>>): Codec<S> =
    dispatchPartial(typeKey, type.andThen(DataResult<A>::success), codec.andThen(DataResult<A>::success))

fun <A : Any, S : Any> Codec<A>.dispatchPartial(
    typeKey: String,
    type: Function<S, DataResult<A>>,
    codec: Function<A, DataResult<MapCodec<S>>>,
): Codec<S> = KeyDispatchCodec(typeKey, this, type::apply, codec::apply).codec()

fun <A : Any> Codec<A>.optionalOf(): Codec<Optional<A>> = OptionalCodec(this)

fun <A : Any> Codec<A>.entryOrList(): Codec<Either<A, List<A>>> = Codec.either(this, this.listOf())

/**
 * 指定された[defaultValue]を初期値に持つ[DefaultedList]を[Codec]に変換します。
 * @param A 値のクラス
 * @param defaultValue [DefaultedList]の初期値
 * @return [DefaultedList]の[Codec]
 */
inline fun <reified A : Any> Codec<List<A>>.defaultedListOf(defaultValue: A): Codec<DefaultedList<A>> =
    xmap({ DefaultedList.copyOf(defaultValue, *it.toTypedArray()) }, Function.identity())

//    DataResult    //

/**
 * 存在する値がある場合はtrueを返し、それ以外の場合はfalseを返します。
 */
val <R : Any> DataResult<R>.isSucceeded: Boolean
    get() = result().isPresent

/**
 * 存在する値がない場合はtrueを返し、それ以外の場合はfalseを返します。
 */
val <R : Any> DataResult<R>.isErrored: Boolean
    get() = error().isPresent

/**
 * [DataResult]が値を保持している場合は指定された[action]をその値で呼び出し、それ以外の場合は何も行いません。
 * @param action 値が存在する場合に実行されるブロック
 */
fun <R : Any> DataResult<R>.onSucceeded(action: (R) -> Unit): DataResult<R> = apply { result().ifPresent(action) }

/**
 * [DataResult]が値を保持していない場合は指定された[action]をその値で呼び出し、それ以外の場合は何も行いません。
 * @param action 値が存在しない場合に実行されるブロック
 */
fun <R : Any> DataResult<R>.onErrored(action: (String) -> Unit): DataResult<R> =
    apply { error().map(DataResult.PartialResult<R>::message).ifPresent(action) }

/**
 * 指定された[predicate]で検証した[DataResult]を返します。
 * @param R 値のクラス
 * @param predicate 値を[Boolean]で評価する。
 * @param errorMessage [predicate]がfalseの場合のエラー文
 * @return [predicate]で評価された[DataResult]
 */
fun <R : Any> DataResult<R>.filter(predicate: (R) -> Boolean, errorMessage: String): DataResult<R> = flatMap { result: R ->
    when (predicate(result)) {
        true -> DataResult.success(result)
        false -> DataResult.error(errorMessage)
    }
}

/**
 * 指定された[predicate]で検証した[DataResult]を返します。
 * @param R 値のクラス
 * @param predicate 値を[Boolean]で評価する。
 * @param errorMessage [predicate]がtrueの場合のエラー文
 * @return [predicate]で評価された[DataResult]
 */
fun <R : Any> DataResult<R>.filterNot(predicate: (R) -> Boolean, errorMessage: String): DataResult<R> = flatMap { result: R ->
    when (predicate(result)) {
        true -> DataResult.error(errorMessage)
        false -> DataResult.success(result)
    }
}

/**
 * 指定された[Optional]を[DataResult]に変換します。
 * @param errorMessage [Optional]が値を保持していない場合のエラー文
 * @return [Optional]が値を保持している場合は[DataResult.success]，それ以外は[DataResult.error]
 */
fun <T : Any> Optional<T>.toDataResult(errorMessage: String): DataResult<T> =
    map(DataResult<T>::success).orElse(DataResult.error(errorMessage))

/**
 * 指定された[T]を[DataResult]に変換します。
 * @param errorMessage [T]がnullの場合のエラー文
 * @return [T]がnullでない場合は[DataResult.success]，それ以外は[DataResult.error]
 */
fun <T : Any> T?.toDataResult(errorMessage: String): DataResult<T> = this?.let(DataResult<T>::success) ?: DataResult.error(errorMessage)

fun <R : Any, T : Any> DataResult<R>.mapNotNull(transform: (R) -> T?): DataResult<T> =
    flatMap { result: R -> transform(result).toDataResult("Transformed value was null!") }

/**
 * 指定された[DataResult]の値を[Optional]に包んで返します。
 */
fun <R : Any> DataResult<R>.getOptional(): Optional<R> = get().left()

/**
 * 指定された[DataResult]の値をnullableな形で返します。
 */
fun <R : Any> DataResult<R>.getOrNull(): R? = getOptional().getOrNull()

/**
 * 指定された[DataResult]の値をnullでない形で返します。
 * @return [getOrNull]がnullの場合，[defaultValue]から返す
 */
fun <R : Any> DataResult<R>.getOrDefault(defaultValue: R): R = getOrNull() ?: defaultValue

/**
 * 指定された[DataResult]の値をnullでない形で返します。
 * @return [getOrNull]がnullの場合，[value]から返す
 */
fun <R : Any> DataResult<R>.getOrElse(value: () -> R): R = getOrNull() ?: value()
