package dev.robustum.core.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.tag.TagManager

fun interface TagsUpdatedEvent {
    fun onUpdated(tagManager: TagManager, isClient: Boolean)

    companion object {
        @JvmField
        val EVENT: Event<TagsUpdatedEvent> = EventFactory.createArrayBacked(TagsUpdatedEvent::class.java) { callbacks ->
            TagsUpdatedEvent {
                    manager,
                    isClient,
                ->
                callbacks.forEach { it.onUpdated(manager, isClient) }
            }
        }
    }
}
