package dev.robustum.core.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier

fun interface TagsBuildingEvent {
    fun register(handler: Handler)

    companion object {
        private fun createEvent(): Event<TagsBuildingEvent> = EventFactory.createArrayBacked(TagsBuildingEvent::class.java) { callbacks ->
            TagsBuildingEvent {
                    handler ->
                callbacks.forEach { it.register(handler) }
            }
        }

        val BLOCK: Event<TagsBuildingEvent> = createEvent()
        val ITEM: Event<TagsBuildingEvent> = createEvent()
        val FLUID: Event<TagsBuildingEvent> = createEvent()
        val ENTITY_TYPE: Event<TagsBuildingEvent> = createEvent()
    }

    class Handler(private val map: MutableMap<Identifier, Tag.Builder>) {
        fun builder(id: Identifier): Tag.Builder = map.computeIfAbsent(id) { Tag.Builder.create() }
    }
}
