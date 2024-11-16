package dev.robustum.core.tag

import com.google.common.collect.ImmutableSet
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import dev.robustum.core.RobustumCore
import dev.robustum.core.extensions.removePrefix
import dev.robustum.core.extensions.removeSuffix
import net.minecraft.resource.Resource
import net.minecraft.resource.ResourceManager
import net.minecraft.tag.Tag
import net.minecraft.tag.TagGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class RobustumTagGroupLoader<T : Any>(private val registry: Registry<T>, private val dataType: String) {
    private val logger: Logger = LogManager.getLogger()
    private val parser = JsonParser()

    private val registryKey: RegistryKey<out Registry<T>> = registry.key

    private fun loadTags(resourceManager: ResourceManager): Map<Identifier, MutableList<TrackedEntry>> =
        buildMap<Identifier, MutableList<TrackedEntry>> {
            resourceManager.findResources(dataType) { it.endsWith(".json") }.forEach { resourceId: Identifier ->
                val fixedId: Identifier = resourceId
                    .removePrefix("$dataType/")
                    .removeSuffix(".json")
                // .let(RobustumTagFormatRegistry::format)
                resourceManager.getAllResources(resourceId).forEach { resource: Resource ->
                    runCatching {
                        val json: JsonElement = resource.use { resourceIn: Resource ->
                            parser.parse(resourceIn.inputStream.bufferedReader())
                        }
                        val builder: MutableList<TrackedEntry> = this.computeIfAbsent(fixedId) { mutableListOf() }
                        val tagFile: RobustumTagFile =
                            RobustumTagFile.CODEC.parse(Dynamic(JsonOps.INSTANCE, json)).getOrThrow(false, logger::error)
                        if (tagFile.replace) {
                            builder.clear()
                        }
                        tagFile.entries.forEach { entry: RobustumTagEntry ->
                            builder.add(
                                TrackedEntry(
                                    entry,
                                    resource.resourcePackName,
                                ),
                            )
                        }
                    }.onFailure(logger::error)
                }
                RuntimeTagCallback.EVENT.invoker().onRegister(
                    RuntimeTagCallback.Helper(registry) { tagId: Identifier, entry: RobustumTagEntry ->
                        this.computeIfAbsent(tagId) { mutableListOf() }.add(TrackedEntry(entry, RobustumCore.MOD_NAME))
                    },
                )
            }
        }.onEach { logger.info("Tag - ${it.key}") }

    private fun buildGroup(rawMap: Map<Identifier, MutableList<TrackedEntry>>): TagGroup<T> = TagGroup.create(
        buildMap {
            val valueGetter = object : RobustumTagEntry.ValueGetter<T> {
                override fun direct(id: Identifier): T? = registry.get(id)

                override fun tag(id: Identifier): Tag<T>? = this@buildMap[id]
            }
            val copiedMap: MutableMap<Identifier, MutableList<TrackedEntry>> = rawMap.toMutableMap()
            while (copiedMap.isNotEmpty()) {
                var result = false
                copiedMap.toList().forEach { (id: Identifier, entries: List<TrackedEntry>) ->
                    resolveEntries(valueGetter, entries)?.let { tag: Tag<T> ->
                        this[id] = tag
                        copiedMap.remove(id)
                        result = true
                    }
                }
                if (!result) {
                    break
                }
            }
            copiedMap.forEach { (id: Identifier, entries: List<TrackedEntry>) ->
                logger.error(
                    "Couldn't load ${registryKey.value.path} tag: $id as it is missing following references: [${
                        getUnresolvedEntries(
                            valueGetter,
                            entries,
                        ).joinToString(separator = ", ", transform = TrackedEntry::toString)
                    }]",
                )
            }
        },
    )

    private fun resolveEntries(valueGetter: RobustumTagEntry.ValueGetter<T>, entries: List<TrackedEntry>): Tag<T>? {
        val setBuilder: ImmutableSet.Builder<T> = ImmutableSet.builder()
        entries.forEach { entry: TrackedEntry ->
            if (!entry.entry.resolve(valueGetter, setBuilder::add)) {
                return null
            }
        }
        return Tag.of(setBuilder.build())
    }

    private fun getUnresolvedEntries(valueGetter: RobustumTagEntry.ValueGetter<T>, entries: List<TrackedEntry>): List<TrackedEntry> =
        entries.filter { !it.entry.resolve(valueGetter) {} }

    fun load(resourceManager: ResourceManager): TagGroup<T> = buildGroup(loadTags(resourceManager))

    //    TrackedEntry    //

    data class TrackedEntry(val entry: RobustumTagEntry, val source: String)
}
