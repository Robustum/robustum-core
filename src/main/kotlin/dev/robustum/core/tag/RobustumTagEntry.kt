package dev.robustum.core.tag

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier

class RobustumTagEntry private constructor(val id: Identifier, val isTag: Boolean, val required: Boolean) {
    companion object {
        @JvmStatic
        private val ENTRY_CODEC: Codec<RobustumTagEntry> = RecordCodecBuilder.create { instance ->
            instance
                .group(
                    RobustumTagCodecs.TAG_ID.fieldOf("id").forGetter(RobustumTagEntry::tagEntryId),
                    Codec.BOOL.optionalFieldOf("required", true).forGetter(RobustumTagEntry::required),
                ).apply(instance, ::RobustumTagEntry)
        }

        @JvmField
        val CODEC: Codec<RobustumTagEntry> = Codec.either(RobustumTagCodecs.TAG_ID, ENTRY_CODEC).xmap(
            { either: Either<RobustumTagCodecs.TagEntryId, RobustumTagEntry> ->
                either.map({
                    RobustumTagEntry(
                        it,
                        true,
                    )
                }, { it })
            },
            {
                when (it.required) {
                    true -> Either.left(it.tagEntryId)
                    false -> Either.right(it)
                }
            },
        )

        @JvmStatic
        fun create(id: Identifier, required: Boolean = true): RobustumTagEntry = RobustumTagEntry(id, false, required)

        @JvmStatic
        fun <T : Any> create(obj: T, transform: (T) -> Identifier, required: Boolean = true): RobustumTagEntry =
            RobustumTagEntry(transform(obj), false, required)

        @JvmStatic
        fun createTag(id: Identifier, required: Boolean = true): RobustumTagEntry = RobustumTagEntry(id, true, required)
    }

    constructor(tagId: RobustumTagCodecs.TagEntryId, required: Boolean) : this(tagId.id, tagId.isTag, required)

    private val tagEntryId = RobustumTagCodecs.TagEntryId(id, isTag)

    fun <T : Any> resolve(valueGetter: ValueGetter<T>, consumer: (T) -> Unit): Boolean {
        if (isTag) {
            valueGetter.tag(id)?.values()?.forEach(consumer) ?: return !required
        } else {
            valueGetter.direct(id)?.let(consumer) ?: return !required
        }
        return true
    }

    override fun toString(): String = "RobustumTagEntry[id=$id, isTag=$isTag, required=$required]"

    //    ValueGetter    //

    interface ValueGetter<T : Any> {
        fun direct(id: Identifier): T?

        fun tag(id: Identifier): Tag<T>?
    }
}
