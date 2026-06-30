package dev.robustum.core.util

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
