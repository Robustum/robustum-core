@file:OptIn(ExperimentalContracts::class)

package dev.robustum.core.extensions

import com.mojang.serialization.Codec
import com.mojang.serialization.Dynamic
import com.mojang.serialization.DynamicOps
import dev.robustum.core.util.DFUPair
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 指定された[DynamicOps]を[Codec]に変換します。
 * @see [dev.robustum.core.codec.RobustumCodecs.ANY]
 */
fun <T : Any> DynamicOps<T>.toCodec(): Codec<T> = Codec.PASSTHROUGH.xmap(
    { dynamic: Dynamic<*> -> dynamic.convert(this).value },
    { obj: T -> Dynamic(this, obj) },
)

//    List    //

/**
 * 指定された[list]を[T]に変換します。
 * @param T 値のクラス
 * @return [T]に変換された[List]
 */
fun <T : Any> DynamicOps<T>.createList(list: List<T>): T = createList(list.stream())

inline fun <T : Any> DynamicOps<T>.createList(builderAction: MutableList<T>.() -> Unit): T {
    contract {
        callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE)
    }
    return createList(buildList(builderAction))
}

//    Map    //

fun <T : Any, K : Any, V : Any> DynamicOps<T>.buildMap(
    keyTransform: (K) -> T,
    valueTransform: (V) -> T,
    builderAction: MutableMap<K, V>.() -> Unit,
): T {
    contract {
        callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE)
    }
    return buildMap(builderAction).map { (key: K, value: V) -> DFUPair(keyTransform(key), valueTransform(value)) }.stream().let(::createMap)
}
