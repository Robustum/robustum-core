package dev.robustum.core.registry

import net.minecraft.util.Identifier

/**
 * レジストリIDとその値を持つインターフェースです。
 * @param T 値のクラス
 * @see [RegistryLookup.getEntry]
 */
sealed interface IdentifiedEntry<T : Any> {
    val id: Identifier
    val value: T
}

internal data class IdentifiedEntryImpl<T : Any>(override val id: Identifier, override val value: T) : IdentifiedEntry<T>
