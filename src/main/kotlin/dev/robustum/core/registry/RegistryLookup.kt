package dev.robustum.core.registry

import com.mojang.serialization.DataResult
import dev.robustum.core.extensions.getIdOrNull
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.tag.ServerTagManagerHolder
import net.minecraft.tag.Tag
import net.minecraft.tag.TagGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

/**
 * [Registry]と[TagGroup]を共通化させたインターフェースです。
 */
interface RegistryLookup<T : Any> {
    /**
     * 指定された[id]から[RegistryEntry]を返します。
     * @return 結果は[DataResult]でラップされます。
     */
    fun getEntry(id: Identifier): DataResult<RegistryEntry<T>>

    /**
     * 指定された[id]から[T]を返します。
     * @return 結果は[DataResult]でラップされます。
     */
    fun getValue(id: Identifier): DataResult<T> = getEntry(id).map(RegistryEntry<T>::value)

    /**
     * 指定された[value]から[TagEntryId]を返します。
     * @return 結果は[DataResult]でラップされます。
     */
    fun getId(value: T): DataResult<TagEntryId>

    /**
     * 指定された[id]から[RegistryEntryList]を返します。
     * @return 結果は[DataResult]でラップされます。
     */
    fun getTag(id: Identifier): DataResult<RegistryEntryList<T>>

    /**
     * 指定された[tag]から[TagEntryId]を返します。
     * @return 結果は[DataResult]でラップされます。
     */
    fun getId(tag: Tag<T>): DataResult<TagEntryId>

    companion object {
        /**
         * [Block]に対する[RegistryLookup]です。
         */
        @JvmField
        val BLOCK: RegistryLookup<Block> =
            of(Registry.BLOCK, ServerTagManagerHolder.getTagManager()::getBlocks)

        /**
         * [Fluid]に対する[RegistryLookup]です。
         */
        @JvmField
        val FLUID: RegistryLookup<Fluid> =
            of(Registry.FLUID, ServerTagManagerHolder.getTagManager()::getFluids)

        /**
         * [EntityType]に対する[RegistryLookup]です。
         */
        @JvmField
        val ENTITY_TYPE: RegistryLookup<EntityType<*>> =
            of(Registry.ENTITY_TYPE, ServerTagManagerHolder.getTagManager()::getEntityTypes)

        /**
         * [Item]に対する[RegistryLookup]です。
         */
        @JvmField
        val ITEM: RegistryLookup<Item> =
            of(Registry.ITEM, ServerTagManagerHolder.getTagManager()::getItems)

        /**
         * [registry]と[groupGetter]から[RegistryLookup]のインスタンスを生成します。
         */
        @JvmStatic
        fun <T : Any> of(registry: Registry<T>, groupGetter: () -> TagGroup<T>): RegistryLookup<T> = object : RegistryLookup<T> {
            override fun getEntry(id: Identifier): DataResult<RegistryEntry<T>> = registry
                .get(id)
                ?.let { RegistryEntryImpl(id, it) }
                ?.let(DataResult<T>::success)
                ?: DataResult.error("Unknown registry id: $id")

            override fun getId(value: T): DataResult<TagEntryId> = registry
                .getId(value)
                ?.let { TagEntryId(it, false) }
                ?.let(DataResult<Identifier>::success)
                ?: DataResult.error("Unknown registry value: $value")

            override fun getTag(id: Identifier): DataResult<RegistryEntryList<T>> = groupGetter()
                .getTag(id)
                ?.let(RegistryEntryList.Companion::ofTag)
                ?.let(DataResult<RegistryEntryList<T>>::success)
                ?: DataResult.error("Unknown tag id: $id")

            override fun getId(tag: Tag<T>): DataResult<TagEntryId> = tag
                .getIdOrNull(groupGetter())
                ?.let { TagEntryId(it, true) }
                ?.let(DataResult<Identifier>::success)
                ?: DataResult.error("Unknown tag: $tag")
        }
    }
}
