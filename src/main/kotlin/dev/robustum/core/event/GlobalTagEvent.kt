package dev.robustum.core.event

import dev.robustum.core.extension.createEvent
import net.fabricmc.fabric.api.event.Event
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier

fun interface GlobalTagEvent {
    fun register(map: MutableMap<Identifier, Tag.Builder>)

    companion object {
        private fun createEvent(): Event<GlobalTagEvent> = createEvent { callbacks ->
            GlobalTagEvent { manager -> callbacks.forEach { it.register(manager) } }
        }

        @JvmStatic
        val BLOCK: Event<GlobalTagEvent> = createEvent()

        @JvmStatic
        val ITEM: Event<GlobalTagEvent> = createEvent()

        @JvmStatic
        val FLUID: Event<GlobalTagEvent> = createEvent()

        @JvmStatic
        val ENTITY_TYPE: Event<GlobalTagEvent> = createEvent()
    }
}
