package dev.robustum.core.tag

import dev.robustum.core.extension.combine
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.tag.Tag
import net.minecraft.tag.TagGroup
import net.minecraft.tag.TagManager
import net.minecraft.util.Identifier

class WrappedTagManager(
    private val manager: TagManager,
    private val extraBlockMap: Map<Identifier, Tag<Block>>,
    private val extraItemMap: Map<Identifier, Tag<Item>>,
    private val extraFluidMap: Map<Identifier, Tag<Fluid>>,
    private val extraEntityTypeMap: Map<Identifier, Tag<EntityType<*>>>,
) : TagManager {
    override fun getBlocks(): TagGroup<Block> = TagGroup.create(
        buildMap {
            putAll(manager.blocks.tags)
            extraBlockMap.forEach { (id: Identifier, tag: Tag<Block>) ->
                compute(id) { _: Identifier, existingTag: Tag<Block>? -> tag.combine(existingTag) }
            }
        },
    )

    override fun getItems(): TagGroup<Item> = TagGroup.create(
        buildMap {
            putAll(manager.items.tags)
            extraItemMap.forEach { (id: Identifier, tag: Tag<Item>) ->
                compute(id) { _: Identifier, existingTag: Tag<Item>? -> tag.combine(existingTag) }
            }
        },
    )

    override fun getFluids(): TagGroup<Fluid> = TagGroup.create(
        buildMap {
            putAll(manager.fluids.tags)
            extraFluidMap.forEach { (id: Identifier, tag: Tag<Fluid>) ->
                compute(id) { _: Identifier, existingTag: Tag<Fluid>? -> tag.combine(existingTag) }
            }
        },
    )

    override fun getEntityTypes(): TagGroup<EntityType<*>> = TagGroup.create(
        buildMap {
            putAll(manager.entityTypes.tags)
            extraEntityTypeMap.forEach { (id: Identifier, tag: Tag<EntityType<*>>) ->
                compute(id) { _: Identifier, existingTag: Tag<EntityType<*>>? -> tag.combine(existingTag) }
            }
        },
    )
}
