package dev.robustum.core.extension

import java.util.*

inline fun <reified T> getInstanceWith(): List<T> = ServiceLoader.load(T::class.java).toList()

inline fun <reified T> getSingleInstanceWith(): T = getInstanceWith<T>().let {
    when {
        it.isEmpty() -> throw IllegalStateException("Any classes implement ${T::class.java.canonicalName}!")
        it.size > 2 -> throw IllegalStateException("Only single class must implement ${T::class.java.canonicalName}!")
        else -> it[0]
    }
}
