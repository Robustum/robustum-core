package dev.robustum.core.registry

import dev.robustum.core.extensions.getIdOrNull
import dev.robustum.core.util.TextResult
import dev.robustum.core.util.toTextResult
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
     * 指定された[id]から[T]を返します。
     * @return 結果は[TextResult]でラップされます。
     */
    operator fun get(id: Identifier): TextResult<T>

    /**
     * 指定された[value]から[TagEntryId]を返します。
     * @return 結果は[TextResult]でラップされます。
     */
    fun getId(value: T): TextResult<TagEntryId>

    /**
     * 指定された[id]から[RegistryEntryList]を返します。
     * @return 結果は[TextResult]でラップされます。
     */
    fun getTag(id: Identifier): TextResult<RegistryEntryList<T>>

    /**
     * 指定された[tag]から[TagEntryId]を返します。
     * @return 結果は[TextResult]でラップされます。
     */
    fun getId(tag: Tag<T>): TextResult<TagEntryId>

    companion object {
        /**
         * [Block]に対する[RegistryLookup]です。
         */
        @JvmField
        val BLOCK: RegistryLookup<Block> = of(Registry.BLOCK) { ServerTagManagerHolder.getTagManager().blocks }

        /**
         * [Fluid]に対する[RegistryLookup]です。
         */
        @JvmField
        val FLUID: RegistryLookup<Fluid> = of(Registry.FLUID) { ServerTagManagerHolder.getTagManager().fluids }

        /**
         * [EntityType]に対する[RegistryLookup]です。
         */
        @JvmField
        val ENTITY_TYPE: RegistryLookup<EntityType<*>> = of(Registry.ENTITY_TYPE) { ServerTagManagerHolder.getTagManager().entityTypes }

        /**
         * [Item]に対する[RegistryLookup]です。
         */
        @JvmField
        val ITEM: RegistryLookup<Item> = of(Registry.ITEM) { ServerTagManagerHolder.getTagManager().items }

        /**
         * [registry]と[groupGetter]から[RegistryLookup]のインスタンスを生成します。
         */
        @JvmStatic
        fun <T : Any> of(registry: Registry<T>, groupGetter: () -> TagGroup<T>): RegistryLookup<T> = object : RegistryLookup<T> {
            override fun get(id: Identifier): TextResult<T> = registry
                .get(id)
                .toTextResult { "Unknown registry id: $id" }

            override fun getId(value: T): TextResult<TagEntryId> = registry
                .getId(value)
                ?.let { TagEntryId(it, false) }
                .toTextResult { "Unknown registry value: $value" }

            override fun getTag(id: Identifier): TextResult<RegistryEntryList<T>> = groupGetter()
                .getTag(id)
                ?.let(RegistryEntryList.Companion::tagged)
                .toTextResult { "Unknown tag id: $id" }

            override fun getId(tag: Tag<T>): TextResult<TagEntryId> = tag
                .getIdOrNull(groupGetter())
                ?.let { TagEntryId(it, true) }
                .toTextResult { "Unknown tag: $tag" }
        }
    }
}
