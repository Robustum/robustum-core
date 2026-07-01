package dev.robustum.core.extensions

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import dev.robustum.core.codec.KeyDispatchCodec
import dev.robustum.core.codec.RobustumCodecs
import dev.robustum.core.util.DFUEither
import dev.robustum.core.util.Either
import dev.robustum.core.util.Option
import dev.robustum.core.util.java
import dev.robustum.core.util.kotlin
import dev.robustum.core.util.left
import dev.robustum.core.util.right
import dev.robustum.core.util.unwrap
import java.util.Optional
import java.util.function.Function

fun <A : Any> Codec<A>.validate(validator: (A) -> DataResult<A>): Codec<A> = flatXmap(validator, validator)

fun <A : Any, E : Any> Codec<A>.dispatchByMap(type: Function<E, A>, codec: Function<A, MapCodec<out E>>): Codec<E> =
    dispatchByMap("type", type, codec)

fun <A : Any, E : Any> Codec<A>.dispatchByMap(typeKey: String, type: Function<E, A>, codec: Function<A, MapCodec<out E>>): Codec<E> =
    dispatchByMapPartial(typeKey, type.andThen { DataResult.success(it) }, codec.andThen { DataResult.success(it) })

fun <A : Any, E : Any> Codec<A>.dispatchByMapPartial(
    typeKey: String,
    type: Function<E, DataResult<A>>,
    codec: Function<A, DataResult<MapCodec<out E>>>,
): Codec<E> = KeyDispatchCodec(typeKey, this, type::apply, codec::apply).codec()

//    List    //

/**
 * この[Codec][this]を[List]の[Codec]に変換します。
 * @param range リストの[長さ][List.size]の範囲
 * @return リストの[長さ][List.size]が制限された[List]の[Codec]
 */
fun <A> Codec<A>.listOf(range: IntRange): Codec<List<A>> = this.listOf(range.first, range.last)

fun <A> Codec<A>.listOf(min: Int, max: Int): Codec<List<A>> = this.listOf().validate { list: List<A> ->
    val size: Int = list.size
    when {
        size < min -> DataResult.error("List is too short: $size, expected range [$min..$max]")
        size > max -> DataResult.error("List is too long: $size, expected range [$min..$max]")
        else -> DataResult.success(list)
    }
}

/**
 * この[Codec][this]を，要素が一つの場合はそのままコーデックする[List]の[Codec]に変換します。
 */
fun <A> Codec<A>.listOrElement(): Codec<List<A>> = RobustumCodecs.either(this.listOf(), this).xmap(
    { either: Either<List<A>, A> -> either.map(::listOf).unwrap() },
    { list: List<A> -> list.singleOrNull()?.right() ?: list.left() },
)

/**
 * この[Codec][this]を，要素が一つの場合はそのままコーデックする[List]の[Codec]に変換します。
 * @param range リストの[長さ][List.size]の範囲
 * @return リストの[長さ][List.size]が制限された[List]の[Codec]
 */
fun <A> Codec<A>.listOrElement(range: IntRange): Codec<List<A>> = this.listOrElement(range.first, range.last)

/**
 * この[Codec][this]を，要素が一つの場合はそのままコーデックする[List]の[Codec]に変換します。
 * @param min リストの[長さ][List.size]の最小値
 * @param max リストの[長さ][List.size]の最大値
 * @return リストの[長さ][List.size]が制限された[List]の[Codec]
 */
fun <A> Codec<A>.listOrElement(min: Int, max: Int): Codec<List<A>> = RobustumCodecs.either(this.listOf(min, max), this).xmap(
    { either: Either<List<A>, A> -> either.map(::listOf).unwrap() },
    { list: List<A> -> list.singleOrNull()?.right() ?: list.left() },
)

//    Set    //

/**
 * この[Codec][this]を[Set]の[Codec]に変換します。
 * @return [Set]の[Codec]
 */
fun <A> Codec<List<A>>.setOf(): Codec<Set<A>> = this.xmap(List<A>::toSet, Set<A>::toList)

//    Either    //

/**
 * [DFUEither]の[MapCodec]を[Either]の[MapCodec]に変換します。
 * @param A 左側の値となるクラス
 * @param B 右側の値となるクラス
 */
@JvmName("convertToEither")
fun <A, B> MapCodec<DFUEither<A, B>>.convert(): MapCodec<Either<A, B>> = this.xmap({ it.kotlin }, { it.java })

//    Option    //

/**
 * [Optional]の[MapCodec]を[Option]の[MapCodec]に変換します。
 * @param A 値のクラス
 */
@JvmName("convertToOption")
fun <A : Any> MapCodec<Optional<A>>.convert(): MapCodec<Option<A>> = this.xmap({ it.kotlin }, { it.java })
