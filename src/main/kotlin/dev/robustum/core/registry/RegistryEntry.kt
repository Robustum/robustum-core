package dev.robustum.core.registry

import net.minecraft.util.Identifier

/**
 * レジストリIDとその値を持つデータクラスです。
 * @param T 値のクラス
 * @param key レジストリID
 * @param value [key]に対応する値
 */
data class RegistryEntry<T : Any>(val key: Identifier, val value: T)
