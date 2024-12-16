package dev.robustum.core.extensions

import com.mojang.datafixers.util.Either
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapCodec
import dev.robustum.core.codec.KeyDispatchCodec
import dev.robustum.core.codec.OptionalCodec
import net.minecraft.util.collection.DefaultedList
import java.util.*
import java.util.function.Function

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

fun <A : Any> alternativeCodec(first: Codec<A>, second: Codec<A>): Codec<A> = Codec.either(first, second).xmap(
    { either: Either<A, A> -> either.map(Function.identity(), Function.identity()) },
    Either<A, A>::left,
)

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
