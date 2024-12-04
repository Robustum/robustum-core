package dev.robustum.core.registry

import com.mojang.serialization.DataResult
import dev.robustum.core.extensions.RobustumCodecs
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.tag.ServerTagManagerHolder
import net.minecraft.tag.Tag
import net.minecraft.tag.TagGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

interface RegistryLookup<T : Any> {
    fun getEntry(id: Identifier): DataResult<RegistryEntry<T>>

    fun getId(value: T): DataResult<RobustumCodecs.TagEntryId>

    fun getTag(id: Identifier): DataResult<RegistryEntryList<T>>

    fun getId(tag: Tag<T>): DataResult<RobustumCodecs.TagEntryId>

    companion object {
        @JvmStatic
        fun <T : Any> of(registry: Registry<T>, groupGetter: () -> TagGroup<T>): RegistryLookup<T> = object : RegistryLookup<T> {
            override fun getEntry(id: Identifier): DataResult<RegistryEntry<T>> = registry
                .get(id)
                ?.let { RegistryEntry(id, it) }
                ?.let(DataResult<T>::success)
                ?: DataResult.error("Unknown registry id: $id")

            override fun getId(value: T): DataResult<RobustumCodecs.TagEntryId> = registry
                .getId(value)
                ?.let { RobustumCodecs.TagEntryId(it, false) }
                ?.let(DataResult<Identifier>::success)
                ?: DataResult.error("Unknown registry value: $value")

            override fun getTag(id: Identifier): DataResult<RegistryEntryList<T>> = groupGetter()
                .getTag(id)
                ?.let(RegistryEntryList.Companion::tag)
                ?.let(DataResult<RegistryEntryList<T>>::success)
                ?: DataResult.error("Unknown tag id: $id")

            override fun getId(tag: Tag<T>): DataResult<RobustumCodecs.TagEntryId> = groupGetter()
                .getUncheckedTagId(tag)
                ?.let { RobustumCodecs.TagEntryId(it, true) }
                ?.let(DataResult<Identifier>::success)
                ?: DataResult.error("Unknown tag: $tag")
        }

        @JvmField
        val BLOCK: RegistryLookup<Block> =
            of(Registry.BLOCK, ServerTagManagerHolder.getTagManager()::getBlocks)

        @JvmField
        val FLUID: RegistryLookup<Fluid> =
            of(Registry.FLUID, ServerTagManagerHolder.getTagManager()::getFluids)

        @JvmField
        val ENTITY_TYPE: RegistryLookup<EntityType<*>> =
            of(Registry.ENTITY_TYPE, ServerTagManagerHolder.getTagManager()::getEntityTypes)

        @JvmField
        val ITEM: RegistryLookup<Item> =
            of(Registry.ITEM, ServerTagManagerHolder.getTagManager()::getItems)
    }
}
