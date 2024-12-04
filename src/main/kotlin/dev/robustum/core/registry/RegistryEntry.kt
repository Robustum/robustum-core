package dev.robustum.core.registry

import net.minecraft.util.Identifier

data class RegistryEntry<T : Any>(val key: Identifier, val value: T)
