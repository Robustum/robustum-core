package dev.robustum.core.tag

import net.fabricmc.fabric.api.tag.TagRegistry
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier

class LazyTag<T : Any> private constructor(private val id: Identifier, private val tagGetter: (Identifier) -> Tag<T>) : Tag.Identified<T> {
    companion object {
        @JvmStatic
        fun block(id: Identifier): LazyTag<Block> = LazyTag(id, TagRegistry::block)

        @JvmStatic
        fun entityType(id: Identifier): LazyTag<EntityType<*>> = LazyTag(id, TagRegistry::entityType)

        @JvmStatic
        fun fluid(id: Identifier): LazyTag<Fluid> = LazyTag(id, TagRegistry::fluid)

        @JvmStatic
        fun item(id: Identifier): LazyTag<Item> = LazyTag(id, TagRegistry::item)
    }

    private val delegatedTag: Tag<T>
        get() = tagGetter(id)

    //    Tag    //

    override fun contains(entry: T): Boolean = delegatedTag.contains(entry)

    override fun values(): List<T> = delegatedTag.values()

    override fun getId(): Identifier = id
}
