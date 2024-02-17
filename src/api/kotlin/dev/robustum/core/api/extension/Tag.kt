package dev.robustum.core.api.extension

import net.minecraft.tag.Tag
import net.minecraft.util.Identifier
import java.util.*

fun <T> buildTagMap(tags: Map<Identifier, Tag.Builder>, registryGetter: (Identifier) -> T?): Map<Identifier, Tag<T>> {
    val map: MutableMap<Identifier, Tag<T>> = hashMapOf()
    while (tags.isNotEmpty()) {
        var bl = false
        val iterator: MutableIterator<Map.Entry<Identifier, Tag.Builder>> = tags.entries.toMutableSet().iterator()
        while (iterator.hasNext()) {
            val entry: Map.Entry<Identifier, Tag.Builder> = iterator.next()
            val optional: Optional<Tag<T>> = entry.value.build(map::get, registryGetter)
            if (!optional.isPresent) continue
            map[entry.key] = optional.get()
            iterator.remove()
            bl = true
        }
        if (bl) continue
        break
    }
    return map
}
