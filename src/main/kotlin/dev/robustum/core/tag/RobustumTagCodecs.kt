package dev.robustum.core.tag

import com.mojang.serialization.Codec
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable

object RobustumTagCodecs {
    @JvmField
    val TAG_ID: Codec<TagEntryId> = Codec.STRING.xmap({
        when (it.startsWith("#")) {
            true -> TagEntryId(Identifier(it.removePrefix("#")), true)
            false -> TagEntryId(Identifier(it), false)
        }
    }, TagEntryId::asString)

    data class TagEntryId(val id: Identifier, val isTag: Boolean) : StringIdentifiable {
        override fun asString(): String = when (isTag) {
            true -> id.toString()
            false -> "#$id"
        }
    }
}
