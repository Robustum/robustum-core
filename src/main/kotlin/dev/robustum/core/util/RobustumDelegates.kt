package dev.robustum.core.util

import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Robustumで使用される委譲プロパティをまとめたクラスです。
 *
 * 参照 : [Kotlin - Delegates][kotlin.properties.Delegates]
 */
data object RobustumDelegates {
    /**
     * 一度だけ値を代入可能なプロパティを作成します。
     * @param T 値のクラス
     */
    fun <T : Any> onceInitialize(): ReadWriteProperty<Any?, T> = OnceInitialize()

    private class OnceInitialize<T : Any> : ReadWriteProperty<Any?, T> {
        private var value: T? = null

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = value ?: error("Property ${property.name} has not initialized")

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            check(this.value == null) { "Property ${property.name} has already initialized" }
            this.value = value
        }
    }
}

fun <T : Any> Registry<T>.lazy(id: Identifier): ReadOnlyProperty<Any?, T> = ReadOnlyProperty { _, _ ->
    this@lazy.get(id)
        ?: error("Missing element: $id")
}
