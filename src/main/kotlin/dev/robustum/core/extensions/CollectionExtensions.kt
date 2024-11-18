package dev.robustum.core.extensions

import net.minecraft.util.collection.DefaultedList

//    DefaultedList    //

inline fun <reified T : Any> Iterable<T>.toDefaultedList(defaultValue: T): DefaultedList<T> =
    DefaultedList<T>.copyOf(defaultValue, *this.toList().toTypedArray())
