package dev.robustum.core.extensions

import com.mojang.serialization.Codec
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable

object RobustumCodecs {
    //    Tag    //

    @JvmField
    val TAG_ID: Codec<TagEntryId> = Codec.STRING.xmap({
        when (it.startsWith("#")) {
            true -> TagEntryId(Identifier(it.removePrefix("#")), true)
            false -> TagEntryId(Identifier(it), false)
        }
    }, TagEntryId::asString)

    data class TagEntryId(val id: Identifier, val isTag: Boolean) : StringIdentifiable {
        companion object {
            @JvmStatic
            fun <T : Any> of(entry: T, transform: (T) -> Identifier): TagEntryId = TagEntryId(transform(entry), false)

            @JvmStatic
            fun <T : Any> tag(tag: Tag<T>, transform: (Tag<T>) -> Identifier): TagEntryId = TagEntryId(transform(tag), true)
        }

        override fun asString(): String = when (isTag) {
            true -> "#$id"
            false -> id.toString()
        }
    }
}
