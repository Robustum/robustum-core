package dev.robustum.core.api.tag

import dev.robustum.core.api.extension.buildTagMap
import net.minecraft.tag.Tag
import net.minecraft.tag.TagManager
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object GlobalTagManager {
    private val blocks: MutableMap<Identifier, Tag.Builder> = hashMapOf()
    private val items: MutableMap<Identifier, Tag.Builder> = hashMapOf()
    private val fluids: MutableMap<Identifier, Tag.Builder> = hashMapOf()
    private val entityTypes: MutableMap<Identifier, Tag.Builder> = hashMapOf()

    @JvmStatic
    fun getBlockBuilder(id: Identifier): Tag.Builder = blocks.computeIfAbsent(id) { Tag.Builder.create() }

    @JvmStatic
    fun getItemBuilder(id: Identifier): Tag.Builder = items.computeIfAbsent(id) { Tag.Builder.create() }

    @JvmStatic
    fun getFluidBuilder(id: Identifier): Tag.Builder = fluids.computeIfAbsent(id) { Tag.Builder.create() }

    @JvmStatic
    fun getEntityTypeBuilder(id: Identifier): Tag.Builder = entityTypes.computeIfAbsent(id) { Tag.Builder.create() }

    @JvmStatic
    fun createWrappedManager(manager: TagManager): TagManager = WrappedTagManager(
        manager,
        buildTagMap(blocks, Registry.BLOCK::get),
        buildTagMap(items, Registry.ITEM::get),
        buildTagMap(fluids, Registry.FLUID::get),
        buildTagMap(entityTypes, Registry.ENTITY_TYPE::get),
    )
}
