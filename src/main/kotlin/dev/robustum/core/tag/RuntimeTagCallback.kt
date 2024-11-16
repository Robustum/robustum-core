package dev.robustum.core.tag

import dev.robustum.core.extensions.idOrNull
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey

fun interface RuntimeTagCallback {
    companion object {
        @JvmField
        val EVENT: Event<RuntimeTagCallback> = EventFactory.createArrayBacked(RuntimeTagCallback::class.java) { callbacks ->
            RuntimeTagCallback { helper: Helper -> callbacks.forEach { it.onRegister(helper) } }
        }
    }

    fun onRegister(helper: Helper)

    //    Helper    //

    class Helper(private val registry: Registry<*>, private val consumer: (Identifier, RobustumTagEntry) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> add(registryKey: RegistryKey<out Registry<T>>, tag: Tag<T>, vararg values: T) {
            val tagId: Identifier = tag.idOrNull ?: return
            if (registryKey == registry.key) {
                val fixedRegistry: Registry<T> = (registry as? Registry<T>) ?: return
                values
                    .mapNotNull(fixedRegistry::getId)
                    .map(RobustumTagEntry.Companion::create)
                    .forEach { consumer(tagId, it) }
            }
        }
    }
}
