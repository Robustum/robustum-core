package dev.robustum.core.text

import com.mojang.serialization.Codec
import dev.robustum.core.codec.RobustumCodecs
import net.minecraft.text.ScoreText
import net.minecraft.util.Identifier

object ScoreTextSerializer : TextSerializer<ScoreText> {
    override val id: Identifier = Identifier("score")
    override val codec: Codec<ScoreText> = RobustumCodecs.record { instance ->
        instance
            .group(
                Codec.STRING.fieldOf("name").forGetter(ScoreText::getName),
                Codec.STRING.fieldOf("objective").forGetter(ScoreText::getObjective),
            ).apply(instance, ::ScoreText)
    }
}
