package dev.robustum.core.extensions

import dev.robustum.core.registry.RegistryEntry
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

//    Registry    //

fun <T : Any> Registry<T>.getEntry(id: Identifier): RegistryEntry<T>? = get(id)?.let { RegistryEntry(id, it) }

fun <T : Any> Registry<T>.getEntryOrThrow(id: Identifier): RegistryEntry<T> = checkNotNull(getEntry(id))

fun <T : Any> Registry<T>.getEntry(value: T): RegistryEntry<T>? = getId(value)?.let { RegistryEntry(it, value) }

fun <T : Any> Registry<T>.getEntryOrThrow(value: T): RegistryEntry<T> = checkNotNull(getEntry(value))
