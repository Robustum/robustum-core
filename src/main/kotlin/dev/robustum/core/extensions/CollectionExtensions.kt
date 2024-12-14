package dev.robustum.core.extensions

import net.minecraft.util.collection.DefaultedList

//    DefaultedList    //

/**
 * [Iterable]を，[defaultValue]を初期値とした[DefaultedList]に変換する
 */
inline fun <reified T : Any> Iterable<T>.toDefaultedList(defaultValue: T): DefaultedList<T> =
    DefaultedList<T>.copyOf(defaultValue, *this.toList().toTypedArray())

/**
 * [Map]を，[defaultValue]を初期値とした[DefaultedList]に[transform]で変換する
 */
inline fun <reified K : Any, reified V : Any, reified T : Any> Map<K, V>.toDefaultedList(
    defaultValue: T,
    transform: (Map.Entry<K, V>) -> T,
): DefaultedList<T> = DefaultedList<T>.copyOf(defaultValue, *this.map(transform).toTypedArray())
