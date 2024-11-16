package dev.robustum.core.tag

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class RobustumTagFile(val entries: List<RobustumTagEntry>, val replace: Boolean) {
    companion object {
        @JvmField
        val CODEC: Codec<RobustumTagFile> = RecordCodecBuilder.create { instance ->
            instance
                .group(
                    RobustumTagEntry.CODEC
                        .listOf()
                        .fieldOf("values")
                        .forGetter(RobustumTagFile::entries),
                    Codec.BOOL.optionalFieldOf("replace", false).forGetter(RobustumTagFile::replace),
                ).apply(instance, ::RobustumTagFile)
        }
    }
}
