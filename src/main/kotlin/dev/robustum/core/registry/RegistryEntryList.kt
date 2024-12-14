package dev.robustum.core.registry

import com.mojang.datafixers.util.Either
import dev.robustum.core.extensions.getSafeValue
import dev.robustum.core.registry.RegistryEntryList.Companion.tag
import net.minecraft.tag.SetTag
import net.minecraft.tag.Tag
import kotlin.random.Random

/**
 * [RegistryEntry]または[Tag]を持つオブジェクトです。
 */
sealed interface RegistryEntryList<T : Any> : Iterable<T> {
    companion object {
        /**
         * 空の[RegistryEntryList]を返します。
         */
        @JvmStatic
        fun <T : Any> empty(): RegistryEntryList<T> = Empty()

        /**
         * [entry]を持つ[RegistryEntryList]を返します。
         */
        @JvmStatic
        fun <T : Any> of(entry: RegistryEntry<T>): RegistryEntryList<T> = Direct(entry)

        /**
         * [value]から[RegistryEntryList]を返します。
         * @param transform [T]を[RegistryEntry]に変換するブロック
         */
        @JvmStatic
        fun <T : Any> of(value: T, transform: (T) -> RegistryEntry<T>): RegistryEntryList<T> = of(transform(value))

        /**
         * [tag]を持つ[RegistryEntryList]を返します。
         */
        @JvmStatic
        fun <T : Any> tag(tag: Tag<T>): RegistryEntryList<T> = Tagged(tag)
    }

    /**
     * [Tag]または[T]を持つ[Either]を返します。
     */
    val storage: Either<Tag<T>, T>

    /**
     * この[RegistryEntryList]の要素が空か判定します。
     */
    val isEmpty: Boolean
        get() = entries.isEmpty()

    /**
     * この[RegistryEntryList]の要素のリストを返します。
     */
    val entries: List<T>
        get() = storage.map(Tag<T>::getSafeValue, ::listOf)

    /**
     * この[RegistryEntryList]の要素の個数を返します。
     */
    val size: Int
        get() = entries.size

    /**
     * この[RegistryEntryList]からランダムな要素を返します。
     */
    fun getRandom(random: Random): T? = entries.randomOrNull(random)

    /**
     * 指定された[index]に対応する要素を返します。
     */
    operator fun get(index: Int): T = entries[index]

    /**
     * 指定された要素[entry]が含まれるか判定します。
     */
    operator fun contains(entry: T): Boolean = storage.map({ it.contains(entry) }, { it == entry })

    override fun iterator(): Iterator<T> = entries.iterator()

    private class Empty<T : Any> : RegistryEntryList<T> {
        override val storage: Either<Tag<T>, T> = Either.left(SetTag.empty())
    }

    private class Direct<T : Any>(entry: RegistryEntry<T>) : RegistryEntryList<T> {
        override val storage: Either<Tag<T>, T> = Either.right(entry.value)
    }

    private class Tagged<T : Any>(tag: Tag<T>) : RegistryEntryList<T> {
        override val storage: Either<Tag<T>, T> = Either.left(tag)
    }
}
