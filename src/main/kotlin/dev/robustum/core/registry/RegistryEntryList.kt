package dev.robustum.core.registry

import dev.robustum.core.util.Either
import dev.robustum.core.util.unwrap
import net.minecraft.tag.Tag

interface RegistryEntryList<out T : Any> : Collection<T> {
    companion object {
        @JvmStatic
        fun <T : Any> empty(): RegistryEntryList<T> = Empty

        @JvmStatic
        fun <T : Any> direct(value: T): RegistryEntryList<T> = direct(listOf(value))

        @JvmStatic
        fun <T : Any> direct(vararg values: T): RegistryEntryList<T> = direct(values.toList())

        @JvmStatic
        fun <T : Any> direct(values: List<T>): RegistryEntryList<T> = when {
            values.isEmpty() -> empty()
            else -> Direct(values)
        }

        @JvmStatic
        fun <T : Any> tagged(tag: Tag<T>): RegistryEntryList<T> = Tagged(tag)
    }

    fun unwrap(): Either<List<T>, Tag<@UnsafeVariance T>>

    fun asList(): List<T> = unwrap().map(Tag<T>::values).unwrap()

    operator fun get(index: Int): T? = asList().getOrNull(index)

    override val size: Int get() = asList().size

    override fun isEmpty(): Boolean = asList().isEmpty()

    override fun contains(element: @UnsafeVariance T): Boolean = element in asList()

    override fun iterator(): Iterator<T> = asList().iterator()

    override fun containsAll(elements: Collection<@UnsafeVariance T>): Boolean = all { contains(it) }

    private data object Empty : RegistryEntryList<Nothing> {
        override fun unwrap(): Either<List<Nothing>, Tag<Nothing>> = Either.Left(listOf())
    }

    @JvmInline
    value class Direct<out T : Any> internal constructor(private val list: List<T>) : RegistryEntryList<T> {
        override fun unwrap(): Either<List<T>, Tag<@UnsafeVariance T>> = Either.Left(list)
    }

    @JvmInline
    value class Tagged<out T : Any> internal constructor(private val tag: Tag<T>) : RegistryEntryList<T> {
        override fun unwrap(): Either<List<T>, Tag<@UnsafeVariance T>> = Either.Right(tag)
    }
}
