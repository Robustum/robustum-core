package dev.robustum.core.registry

import com.mojang.datafixers.util.Either
import dev.robustum.core.extensions.getSafeValue
import net.minecraft.tag.SetTag
import net.minecraft.tag.Tag
import kotlin.random.Random

sealed interface RegistryEntryList<T : Any> : Iterable<T> {
    companion object {
        @JvmStatic
        fun <T : Any> empty(): RegistryEntryList<T> = Empty()

        @JvmStatic
        fun <T : Any> of(entry: RegistryEntry<T>): RegistryEntryList<T> = Direct(entry)

        @JvmStatic
        fun <T : Any> of(value: T, transform: (T) -> RegistryEntry<T>): RegistryEntryList<T> = of(transform(value))

        @JvmStatic
        fun <T : Any> tag(tag: Tag<T>): RegistryEntryList<T> = Tagged(tag)
    }

    val storage: Either<Tag<T>, T>

    val isEmpty: Boolean
        get() = entries.isEmpty()

    val entries: List<T>
        get() = storage.map(Tag<T>::getSafeValue, ::listOf)

    val size: Int
        get() = entries.size

    fun getRandom(random: Random): T? = entries.randomOrNull(random)

    operator fun get(index: Int): T = entries[index]

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
