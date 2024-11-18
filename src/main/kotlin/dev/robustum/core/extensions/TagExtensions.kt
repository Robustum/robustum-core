package dev.robustum.core.extensions

import net.minecraft.tag.Tag
import net.minecraft.util.Identifier

//    Tag    //

val <T : Any> Tag<T>.idOrNull: Identifier?
    get() = (this as? Tag.Identified<T>)?.id

fun <T : Any> Tag<T>.getSafeValue(): List<T> = runCatching { values() }.getOrDefault(listOf())
