package dev.robustum.core.recipe

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import dev.robustum.core.extensions.RobustumCodecs
import net.minecraft.tag.Tag
import net.minecraft.tag.TagGroup
import net.minecraft.util.registry.DefaultedRegistry

sealed interface RegistryEntryList<T : Any> : Iterable<T> {
    companion object {
        @JvmStatic
        fun <T : Any> codec(registry: DefaultedRegistry<T>, groupGetter: () -> TagGroup<T>): Codec<RegistryEntryList<T>> =
            RobustumCodecs.TAG_ID.xmap(
                {
                    when (it.isTag) {
                        true -> Tagged(groupGetter().getTagOrEmpty(it.id))
                        false -> Direct(checkNotNull(registry.get(it.id)))
                    }
                },
                {
                    it.storage.map(
                        { tag: Tag<T> -> RobustumCodecs.TagEntryId.tag(tag, groupGetter()::getTagId) },
                        { entry: T -> RobustumCodecs.TagEntryId.of(entry, registry::getId) },
                    )
                },
            )

        @JvmStatic
        fun <T : Any> of(entry: T): RegistryEntryList<T> = Direct(entry)

        @JvmStatic
        fun <T : Any> tag(tag: Tag<T>): RegistryEntryList<T> = Tagged(tag)
    }

    val storage: Either<Tag<T>, T>

    private class Direct<T : Any>(private val entry: T) : RegistryEntryList<T> {
        override val storage: Either<Tag<T>, T> = Either.right(entry)

        override fun iterator(): Iterator<T> = listOf(entry).iterator()
    }

    private class Tagged<T : Any>(val tag: Tag<T>) : RegistryEntryList<T> {
        override val storage: Either<Tag<T>, T> = Either.left(tag)

        override fun iterator(): Iterator<T> = tag.values().iterator()
    }
}
