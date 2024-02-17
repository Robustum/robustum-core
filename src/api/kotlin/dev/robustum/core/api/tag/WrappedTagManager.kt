package dev.robustum.core.api.tag

import dev.robustum.core.api.extension.buildTagMap
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.tag.Tag
import net.minecraft.tag.TagGroup
import net.minecraft.tag.TagManager
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class WrappedTagManager
    @JvmOverloads
    constructor(
        private val manager: TagManager,
        private val extraBlockGroup: Map<Identifier, Tag.Builder> = mapOf(),
        private val extraItemGroup: Map<Identifier, Tag.Builder> = mapOf(),
        private val extraFluidGroup: Map<Identifier, Tag.Builder> = mapOf(),
        private val extraEntityTypeGroup: Map<Identifier, Tag.Builder> = mapOf(),
    ) : TagManager {
        override fun getBlocks(): TagGroup<Block> = TagGroup.create(
            buildMap {
                putAll(manager.blocks.tags)
                putAll(buildTagMap(extraBlockGroup, Registry.BLOCK::get))
            },
        )

        override fun getItems(): TagGroup<Item> = TagGroup.create(
            buildMap {
                putAll(manager.items.tags)
                putAll(buildTagMap(extraItemGroup, Registry.ITEM::get))
            },
        )

        override fun getFluids(): TagGroup<Fluid> = TagGroup.create(
            buildMap {
                putAll(manager.fluids.tags)
                putAll(buildTagMap(extraFluidGroup, Registry.FLUID::get))
            },
        )

        override fun getEntityTypes(): TagGroup<EntityType<*>> = TagGroup.create(
            buildMap {
                putAll(manager.entityTypes.tags)
                putAll(buildTagMap(extraEntityTypeGroup, Registry.ENTITY_TYPE::get))
            },
        )
    }
