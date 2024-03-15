package dev.robustum.core.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.robustum.core.event.TagsBuildingEvent
import net.minecraft.resource.Resource
import net.minecraft.resource.ResourceManager
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.InputStream

object TagGroupLoaderMixinImpl {
    private val logger = LogManager.getLogger(TagGroupLoaderMixinImpl::class.java)
    private val gson = Gson()

    fun loadTagMap(manager: ResourceManager, dataType: String): Map<Identifier, Tag.Builder> {
        val result: MutableMap<Identifier, Tag.Builder> = hashMapOf()
        manager.findResources(dataType) { it.endsWith(".json") }.forEach { id: Identifier ->
            // TODo: Replace below line with Identifier.modify((String) -> String)
            val tagId = Identifier(id.namespace, id.path.removePrefix("$dataType/").removeSuffix(".json"))
            manager.getAllResources(id).forEach { resource: Resource ->
                resource.use {
                    resource
                        .let(Resource::getInputStream)
                        .let(InputStream::bufferedReader)
                        .let { reader: BufferedReader -> JsonHelper.deserialize(gson, reader, JsonObject::class.java) }
                        ?.let { jsonObject: JsonObject ->
                            result.computeIfAbsent(tagId) { Tag.Builder.create() }
                                .read(jsonObject, resource.resourcePackName)
                        }
                }
            }
        }
        // Invoke TagsBuildingEvent
        logger.info("Current loading tag type; $dataType")
        when (dataType) {
            "tags/blocks" -> TagsBuildingEvent.BLOCK
            "tags/items" -> TagsBuildingEvent.ITEM
            "tags/fluids" -> TagsBuildingEvent.FLUID
            "tags/entity_types" -> TagsBuildingEvent.ENTITY_TYPE
            else -> null
        }?.invoker()?.register(TagsBuildingEvent.Handler(result))
        // Remove empty builder
        HashMap(result).forEach { (id: Identifier, builder: Tag.Builder) ->
            if (!builder.streamEntries().findAny().isPresent) {
                result.remove(id)
            }
        }
        logger.info("Removed empty tag builders!")
        return result
    }
}
