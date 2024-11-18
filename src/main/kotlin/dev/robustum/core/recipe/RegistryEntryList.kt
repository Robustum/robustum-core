package dev.robustum.core.recipe

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import dev.robustum.core.extensions.RobustumCodecs
import dev.robustum.core.extensions.getSafeValue
import net.minecraft.tag.Tag
import net.minecraft.tag.TagGroup
import net.minecraft.util.registry.DefaultedRegistry
import java.util.function.Function
import kotlin.random.Random

sealed interface RegistryEntryList<T : Any> : Iterable<T> {
    companion object {
        @JvmStatic
        fun <T : Any> codec(registry: DefaultedRegistry<T>, groupGetter: () -> TagGroup<T>): Codec<RegistryEntryList<T>> = Codec
            .either(RobustumCodecs.TAG_ID, RobustumCodecs.TAG_ID.listOf())
            .xmap(
                { either: Either<RobustumCodecs.TagEntryId, List<RobustumCodecs.TagEntryId>> ->
                    either.map(
                        { entry: RobustumCodecs.TagEntryId ->
                            when (entry.isTag) {
                                true -> tag(groupGetter().getTagOrEmpty(entry.id))
                                false -> of(checkNotNull(registry.get(entry.id)))
                            }
                        },
                        { entries: List<RobustumCodecs.TagEntryId> ->
                            when {
                                entries.isEmpty() -> empty()
                                !entries.all(RobustumCodecs.TagEntryId::isTag) -> of(
                                    entries.map(RobustumCodecs.TagEntryId::id).map(registry::get),
                                )

                                else -> throw IllegalStateException("Could not serialize TagEntryId list containing tag entry!!")
                            }
                        },
                    )
                },
                { entryList: RegistryEntryList<T> ->
                    entryList.storage.map(
                        { Either.left(RobustumCodecs.TagEntryId.tag(it, groupGetter()::getTagId)) },
                        { entries: List<T> ->
                            when (entries.size) {
                                0 -> throw IllegalStateException("Could not serialize empty list to TagEntryId!")
                                1 -> Either.left(RobustumCodecs.TagEntryId.of(entries[0], registry::getId))
                                else -> Either.right(
                                    entries.map { entry: T ->
                                        RobustumCodecs.TagEntryId.of(entry, registry::getId)
                                    },
                                )
                            }
                        },
                    )
                },
            )

        @JvmStatic
        fun <T : Any> empty(): RegistryEntryList<T> = Direct(listOf())

        @JvmStatic
        fun <T : Any> of(vararg entries: T): RegistryEntryList<T> = Direct(entries.toList())

        @JvmStatic
        fun <T : Any> of(entries: List<T>): RegistryEntryList<T> = Direct(entries.toList())

        @JvmStatic
        fun <T : Any> tag(tag: Tag<T>): RegistryEntryList<T> = Tagged(tag)
    }

    val storage: Either<Tag<T>, List<T>>

    val isEmpty: Boolean
        get() = entries.isEmpty()

    val entries: List<T>
        get() = storage.map(Tag<T>::getSafeValue, Function.identity())

    val size: Int
        get() = entries.size

    fun getRandom(random: Random): T? = entries.randomOrNull(random)

    operator fun get(index: Int): T = entries[index]

    operator fun contains(entry: T): Boolean = storage.map({ it.contains(entry) }, { it.any { entryIn: T -> entryIn == entry } })

    private class Direct<T : Any>(private val list: List<T>) : RegistryEntryList<T> {
        override val storage: Either<Tag<T>, List<T>> = Either.right(list)

        override fun iterator(): Iterator<T> = list.iterator()
    }

    private class Tagged<T : Any>(val tag: Tag<T>) : RegistryEntryList<T> {
        override val storage: Either<Tag<T>, List<T>> = Either.left(tag)

        override fun iterator(): Iterator<T> = tag.getSafeValue().iterator()
    }
}
