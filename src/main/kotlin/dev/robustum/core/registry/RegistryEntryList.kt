package dev.robustum.core.registry

import dev.robustum.core.extensions.getSafeValue
import dev.robustum.core.util.Either
import dev.robustum.core.util.identity
import net.minecraft.tag.Tag
import kotlin.random.Random

/**
 * [T]値の[List]または[Tag]を持つオブジェクトです。
 */

sealed interface RegistryEntryList<out T : Any> : Iterable<T> {
    companion object {
        /**
         * 空の[RegistryEntryList]を返します。
         */
        @JvmStatic
        fun <T : Any> empty(): RegistryEntryList<T> = Empty

        /**
         * [value]を持つ[RegistryEntryList]を返します。
         */
        @JvmStatic
        fun <T : Any> direct(value: T): RegistryEntryList<T> = direct(listOf(value))

        /**
         * [values]を持つ[RegistryEntryList]を返します。
         */
        @JvmStatic
        fun <T : Any> direct(vararg values: T): RegistryEntryList<T> = direct(values.toList())

        /**
         * [values]を持つ[RegistryEntryList]を返します。
         */
        @JvmStatic
        fun <T : Any> direct(values: List<T>): RegistryEntryList<T> = Direct(values)

        /**
         * [tag]を持つ[RegistryEntryList]を返します。
         */
        @JvmStatic
        fun <T : Any> ofTag(tag: Tag<T>): RegistryEntryList<T> = Tagged(tag)
    }

    /**
     * [Tag]または[List]を持つ[Either]を返します。
     */
    fun unwrap(): Either<Tag<@UnsafeVariance T>, List<@UnsafeVariance T>>

    /**
     * この[RegistryEntryList]の要素が空か判定します。
     */
    val isEmpty: Boolean get() = entries.isEmpty()

    /**
     * この[RegistryEntryList]の要素の個数を返します。
     */
    val size: Int get() = entries.size

    /**
     * この[RegistryEntryList]の要素のリストを返します。
     */
    val entries: List<T> get() = unwrap().fold(Tag<T>::getSafeValue, identity())

    /**
     * この[RegistryEntryList]からランダムな要素を返します。
     */
    fun getRandom(random: Random): T? = entries.randomOrNull(random)

    /**
     * 指定された[index]に対応する要素を返します。
     */
    operator fun get(index: Int): T? = entries.getOrNull(index)

    /**
     * 指定された要素[entry]が含まれるか判定します。
     */
    operator fun contains(entry: @UnsafeVariance T): Boolean = entries.contains(entry)

    override fun iterator(): Iterator<T> = entries.iterator()

    private data object Empty : RegistryEntryList<Nothing> {
        override fun unwrap(): Either<Tag<Nothing>, List<Nothing>> = Either.Right(listOf())
    }

    @JvmInline
    value class Direct<out T : Any>(private val values: List<T>) : RegistryEntryList<T> {
        override fun unwrap(): Either<Tag<@UnsafeVariance T>, List<@UnsafeVariance T>> = Either.Right(values)
    }

    @JvmInline
    value class Tagged<out T : Any>(private val tag: Tag<T>) : RegistryEntryList<T> {
        override fun unwrap(): Either<Tag<@UnsafeVariance T>, List<@UnsafeVariance T>> = Either.Left(tag)
    }
}
