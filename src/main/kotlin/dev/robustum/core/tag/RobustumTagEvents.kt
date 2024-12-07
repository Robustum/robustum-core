package dev.robustum.core.tag

import dev.robustum.core.extensions.idOrNull
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.tag.Tag
import net.minecraft.tag.TagManager
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import java.util.function.BiConsumer

object RobustumTagEvents {
    @JvmField
    val REGISTER: Event<Register> =
        EventFactory.createArrayBacked(Register::class.java) { callbacks: Array<out Register> ->
            Register { helper: Helper -> callbacks.forEach { it.onRegister(helper) } }
        }

    @JvmField
    val RELOAD: Event<Reload> = EventFactory.createArrayBacked(Reload::class.java) { callbacks: Array<out Reload> ->
        Reload { manager: TagManager, environment: EnvType -> callbacks.forEach { it.onReload(manager, environment) } }
    }

    //    Register    //

    fun interface Register {
        fun onRegister(helper: Helper)
    }

    //    Helper    //

    class Helper(private val registry: Registry<*>, private val consumer: BiConsumer<Identifier, Tag.Entry>) {
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> add(registryKey: RegistryKey<out Registry<T>>, tag: Tag<T>, vararg values: T) {
            val tagId: Identifier = tag.idOrNull ?: return
            if (registryKey == registry.key) {
                val fixedRegistry: Registry<T> = (registry as? Registry<T>) ?: return
                values
                    .mapNotNull(fixedRegistry::getId)
                    .map { Tag.ObjectEntry(it) }
                    .forEach { consumer.accept(tagId, it) }
            }
        }
    }

    //    Reload    //

    fun interface Reload {
        fun onReload(manager: TagManager, environment: EnvType)
    }
}
