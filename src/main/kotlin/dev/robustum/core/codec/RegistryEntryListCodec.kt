package dev.robustum.core.codec

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.robustum.core.extensions.listOrElement
import dev.robustum.core.registry.RegistryEntryList
import dev.robustum.core.registry.RegistryLookup
import dev.robustum.core.registry.TagEntryId
import dev.robustum.core.util.DFUPair
import dev.robustum.core.util.Either
import dev.robustum.core.util.toDataResult
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.tag.Tag
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.streams.asStream

class RegistryEntryListCodec<A : Any>(private val lookup: RegistryLookup<A>) : Codec<RegistryEntryList<A>> {
    companion object {
        @JvmField
        val BLOCK: RegistryEntryListCodec<Block> = RegistryEntryListCodec(RegistryLookup.BLOCK)

        @JvmField
        val FLUID: RegistryEntryListCodec<Fluid> = RegistryEntryListCodec(RegistryLookup.FLUID)

        @JvmField
        val ENTITY_TYPE: RegistryEntryListCodec<EntityType<*>> =
            RegistryEntryListCodec(RegistryLookup.ENTITY_TYPE)

        @JvmField
        val ITEM: RegistryEntryListCodec<Item> = RegistryEntryListCodec(RegistryLookup.ITEM)

        private val logger: Logger = LogManager.getLogger(RegistryEntryListCodec::class.java)
    }

    private val entryCodec: Codec<List<TagEntryId>> = TagEntryId.CODEC.listOrElement()

    override fun <T : Any> encode(input: RegistryEntryList<A>, ops: DynamicOps<T>, prefix: T): DataResult<T> = input.unwrap().fold(
        { tag: Tag<A> ->
            lookup
                .getId(tag)
                .map(TagEntryId::asString)
                .map(ops::createString)
                .toDataResult()
        },
        { entries: List<A> ->
            when (entries.size) {
                0 -> DataResult.success<T>(ops.emptyList())

                1 ->
                    lookup
                        .getId(entries[0])
                        .map(TagEntryId::asString)
                        .map(ops::createString)
                        .toDataResult()

                else -> {
                    entries
                        .map(lookup::getId)
                        .map { it.map(TagEntryId::asString).map(ops::createString) }
                        .asSequence()
                        .mapNotNull { it.getOrNull() }
                        .asStream()
                        .let(ops::createList)
                        .let(DataResult<T>::success)
                }
            }
        },
    )

    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<DFUPair<RegistryEntryList<A>, T>> =
        entryCodec.decode(ops, input).map { pair: DFUPair<Either<TagEntryId, List<TagEntryId>>, T> ->
            pair.mapFirst { either: Either<TagEntryId, List<TagEntryId>> ->
                either.fold(
                    { entry: TagEntryId ->
                        when (entry.isTag) {
                            true -> lookup.getTag(entry.id)
                            false ->
                                lookup
                                    .getValue(entry.id)
                                    .map(RegistryEntryList.Companion::direct)
                        }.getOrThrow(false, logger::error)
                    },
                    { entries: List<TagEntryId> ->
                        when {
                            entries.isEmpty() -> RegistryEntryList.empty<A>()
                            entries.none(TagEntryId::isTag) -> {
                                entries
                                    .map(TagEntryId::id)
                                    .map(lookup::getValue)
                                    .map { result: DataResult<A> ->
                                        result.getOrThrow(false, logger::error)
                                    }.let(RegistryEntryList.Companion::direct)
                            }

                            else -> RegistryEntryList.empty()
                        }
                    },
                )
            }
        }
}
