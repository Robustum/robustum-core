package dev.robustum.core.registry

import com.mojang.datafixers.util.Either
import dev.robustum.core.extensions.getSafeValue
import net.minecraft.tag.Tag
import java.util.function.Function
import kotlin.random.Random

/**
 * [T]値の[List]または[Tag]を持つオブジェクトです。
 */

sealed interface RegistryEntryList<T : Any> : Iterable<T> {
    companion object {
        /**
         * 空の[RegistryEntryList]を返します。
         */
        @JvmStatic
        fun <T : Any> empty(): RegistryEntryList<T> = Empty()

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
    val storage: Either<Tag<T>, List<T>>

    /**
     * この[RegistryEntryList]の要素が空か判定します。
     */
    val isEmpty: Boolean
        get() = entries.isEmpty()

    /**
     * この[RegistryEntryList]の要素の個数を返します。
     */
    val size: Int
        get() = entries.size

    /**
     * この[RegistryEntryList]の要素のリストを返します。
     */
    val entries: List<T>
        get() = storage.map(Tag<T>::getSafeValue, Function.identity())

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
    operator fun contains(entry: T): Boolean = entries.contains(entry)

    override fun iterator(): Iterator<T> = entries.iterator()

    private class Empty<T : Any> : RegistryEntryList<T> {
        override val storage: Either<Tag<T>, List<T>> = Either.right(listOf())
    }

    private class Direct<T : Any>(values: List<T>) : RegistryEntryList<T> {
        override val storage: Either<Tag<T>, List<T>> = Either.right(values)
    }

    private class Tagged<T : Any>(tag: Tag<T>) : RegistryEntryList<T> {
        override val storage: Either<Tag<T>, List<T>> = Either.left(tag)
    }
}
