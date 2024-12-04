package dev.robustum.core.registry

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.robustum.core.extensions.RobustumCodecs
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item

class RegistryEntryListCodec<A : Any>(private val lookup: RegistryLookup<A>) : Codec<RegistryEntryList<A>> {
    companion object {
        @JvmField
        val BLOCK: RegistryEntryListCodec<Block> = RegistryEntryListCodec(RegistryLookup.BLOCK)

        @JvmField
        val FLUID: RegistryEntryListCodec<Fluid> = RegistryEntryListCodec(RegistryLookup.FLUID)

        @JvmField
        val ENTITY_TYPE: RegistryEntryListCodec<EntityType<*>> = RegistryEntryListCodec(RegistryLookup.ENTITY_TYPE)

        @JvmField
        val ITEM: RegistryEntryListCodec<Item> = RegistryEntryListCodec(RegistryLookup.ITEM)
    }

    override fun <T : Any> encode(input: RegistryEntryList<A>, ops: DynamicOps<T>, prefix: T): DataResult<T> = input.storage
        .map(lookup::getId, lookup::getId)
        .map(RobustumCodecs.TagEntryId::asString)
        .map(ops::createString)

    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<RegistryEntryList<A>, T>> = RobustumCodecs.TAG_ID
        .decode(ops, input)
        .flatMap { pair: Pair<RobustumCodecs.TagEntryId, T> ->
            val tagEntry: RobustumCodecs.TagEntryId = pair.first
            when (tagEntry.isTag) {
                true -> lookup.getTag(tagEntry.id)
                false -> lookup.getEntry(tagEntry.id).map(RegistryEntryList.Companion::of)
            }.map { entryList: RegistryEntryList<A> -> Pair.of(entryList, pair.second) }
        }
}
