package dev.robustum.core.extensions

import net.minecraft.util.collection.DefaultedList

//    DefaultedList    //

inline fun <reified T : Any> Iterable<T>.toDefaultedList(defaultValue: T): DefaultedList<T> =
    DefaultedList<T>.copyOf(defaultValue, *this.toList().toTypedArray())

inline fun <reified K : Any, reified V : Any, reified T : Any> Map<K, V>.toDefaultedList(
    defaultValue: T,
    transform: (Map.Entry<K, V>) -> T,
): DefaultedList<T> = DefaultedList<T>.copyOf(defaultValue, *this.map(transform).toTypedArray())
