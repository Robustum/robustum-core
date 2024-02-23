package dev.robustum.core.extension

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

inline fun <reified T> createEvent(noinline factory: (Array<T>) -> T): Event<T> = EventFactory.createArrayBacked(T::class.java, factory)
