package dev.robustum.core.extensions

import com.mojang.serialization.DynamicOps

//    List    //

/**
 * 指定された[list]を[T]に変換します。
 * @param T 値のクラス
 * @return [T]に変換された[List]
 */
fun <T : Any> DynamicOps<T>.createList(list: List<T>): T = createList(list.stream())

/**
 * 指定された[entries]を[T]に変換します。
 * @param T 値のクラス
 * @return [T]に変換された[List]
 */
fun <T : Any> DynamicOps<T>.createList(vararg entries: T): T = createList(entries.toList())

/**
 * 指定された[list]を[transform]で[T]に変換します。
 * @param T 値のクラス
 * @param transform [E]を[T]に変換する
 * @return [T]に変換された[List]
 */
fun <T : Any, E : Any> DynamicOps<T>.createList(transform: (E) -> T, list: List<E>): T = createList(list.map(transform))

/**
 * 指定された[entries]を[transform]で[T]に変換します。
 * @param T 値のクラス
 * @param transform [E]を[T]に変換する
 * @return [T]に変換された[List]
 */
fun <T : Any, E : Any> DynamicOps<T>.createList(transform: (E) -> T, vararg entries: E): T = createList(entries.map(transform))

fun <T : Any, E : Any> DynamicOps<T>.buildList(transform: (E) -> T, builderAction: MutableList<E>.() -> Unit): T =
    createList(buildList(builderAction).map(transform))

//    Map    //

fun <T : Any, K : Any, V : Any> DynamicOps<T>.buildMap(
    keyTransform: (K) -> T,
    valueTransform: (V) -> T,
    builderAction: MutableMap<K, V>.() -> Unit,
): T = createMap(buildMap(builderAction).mapKeys { keyTransform(it.key) }.mapValues { valueTransform(it.value) })
