package dev.robustum.core.extensions

import net.minecraft.util.collection.DefaultedList

//    DefaultedList    //

/**
 * [Iterable]を[DefaultedList]に変換します。
 * @param T 値のクラス
 * @param defaultValue [DefaultedList]の初期値
 * @return [defaultValue]を初期値に持つ[DefaultedList]
 */
inline fun <reified T : Any> Iterable<T>.toDefaultedList(defaultValue: T): DefaultedList<T> =
    DefaultedList<T>.copyOf(defaultValue, *this.toList().toTypedArray())

/**
 * [Map]を[DefaultedList]で変換する
 * @param T 値のクラス
 * @param defaultValue [DefaultedList]の初期値
 * @param transform [Map.Entry]を[T]に変換するブロック
 * @return [defaultValue]を初期値に持つ[DefaultedList]
 */
inline fun <reified K : Any, reified V : Any, reified T : Any> Map<K, V>.toDefaultedList(
    defaultValue: T,
    transform: (Map.Entry<K, V>) -> T,
): DefaultedList<T> = DefaultedList<T>.copyOf(defaultValue, *this.map(transform).toTypedArray())
