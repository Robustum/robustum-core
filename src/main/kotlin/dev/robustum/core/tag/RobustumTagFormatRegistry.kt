package dev.robustum.core.tag

import dev.robustum.core.extensions.modify
import net.minecraft.util.Identifier

object RobustumTagFormatRegistry {
    @JvmStatic
    private val registry: MutableList<TagFormatter> = mutableListOf()

    @JvmStatic
    fun register(formatter: TagFormatter) {
        registry.add(formatter)
    }

    @JvmStatic
    fun format(id: Identifier): Identifier {
        if (id.namespace == "minecraft") return id
        registry.forEach { formatter: TagFormatter ->
            if (formatter.canFormat(id.path)) {
                return id.modify(formatter::format)
            }
        }
        return id
    }

    init {
        register(TagFormatter.conventional("blocks"))
        register(TagFormatter.conventional("bricks"))
        register(TagFormatter.conventional("ingots"))
        register(TagFormatter.conventional("logs"))
        register(TagFormatter.conventional("nuggets"))
        register(TagFormatter.conventional("ores"))
    }
}
